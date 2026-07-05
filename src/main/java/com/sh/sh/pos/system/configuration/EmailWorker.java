package com.sh.sh.pos.system.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.storemailconfigs.StoreMailConfig;
import com.sh.sh.pos.system.payload.dto.emaildto.EmailDTO;
import com.sh.sh.pos.system.repository.purchasesorderrepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.repository.storemailconfigsrepository.StoreMailConfigRepository;
import com.sh.sh.pos.system.service.emailservice.DynamicMailSender;
import com.sh.sh.pos.system.service.purchaseorderspdfservice.PurchaseOrderPdfService;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailWorker {

    private final RedisTemplate<String, EmailDTO> redisTemplate;
    private final JavaMailSender                  mailSender;
    private final DynamicMailSender               dynamicMailSender;
    private final StoreMailConfigRepository       mailConfigRepository;
    private final PurchaseOrderRepository         purchaseOrderRepository;
    private final PurchaseOrderPdfService         pdfService;

    @Value("${app.mail.from:noreply@example.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:SH-POS System}")
    private String fromName;

    private static final String EMAIL_QUEUE = "email-queue";
    private static final String EMAIL_QUEUE_FAILED = "email-queue-failed";

    public EmailWorker(
            @Qualifier("emailRedisTemplate") RedisTemplate<String, EmailDTO> redisTemplate,
            JavaMailSender mailSender,
            DynamicMailSender dynamicMailSender,
            StoreMailConfigRepository mailConfigRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderPdfService pdfService) {
        this.redisTemplate           = redisTemplate;
        this.mailSender              = mailSender;
        this.dynamicMailSender       = dynamicMailSender;
        this.mailConfigRepository    = mailConfigRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.pdfService              = pdfService;
    }

    // ── Consumer ──────────────────────────────────────────────────────────────

    @Scheduled(fixedDelay = 3000)
    public void consumeQueue() {
        try {
            EmailDTO dto = redisTemplate.opsForList().rightPop(EMAIL_QUEUE);
            if (dto == null) {
                return;
            }

            log.info("📬 Dequeued → to={} storeId={} purchaseOrderId={}",
                    dto.getTo(), dto.getStoreId(), dto.getPurchaseOrderId());

            String to = sanitizeAddress(dto.getTo());
            if (to == null) {
                log.warn("⚠️  Skipped — recipient invalid or blank: raw='{}'", dto.getTo());
                return;
            }
            dto.setTo(to);

            PdfResult pdf = generatePdfIfNeeded(dto);

            if (dto.getStoreId() != null) {
                sendViaStoreConfig(dto, pdf.bytes(), pdf.name());
            } else {
                sendViaGlobalConfig(dto, pdf.bytes(), pdf.name());
            }

        } catch (Exception e) {
            log.error("❌ consumeQueue error: {}", e.getMessage(), e);
        }
    }

    /**
     * Trims whitespace and strips trailing separators. Returns null if nothing
     * usable remains — a trailing comma/semicolon in a stored address otherwise
     * produces JavaMail's "Empty address in string" error.
     */
    private String sanitizeAddress(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim();
        while (cleaned.endsWith(",") || cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        return cleaned.isBlank() ? null : cleaned;
    }

    // ── PDF generation ─────────────────────────────────────────────────────────

    private PdfResult generatePdfIfNeeded(EmailDTO dto) {
        if (dto.getPurchaseOrderId() == null) {
            return PdfResult.empty();
        }

        try {
            PurchaseOrder order = purchaseOrderRepository
                    .findByIdWithAllRelations(dto.getPurchaseOrderId())
                    .orElse(null);

            if (order == null) {
                log.error("❌ PO not found: id={}", dto.getPurchaseOrderId());
                return PdfResult.empty();
            }

            log.info("✅ PO loaded → poNumber={} items={}",
                    order.getPoNumber(),
                    order.getItems() != null ? order.getItems().size() : 0);

            return buildPdfResult(order);

        } catch (Exception e) {
            log.error("❌ PDF generation failed: {}", e.getMessage(), e);
            return PdfResult.empty();
        }
    }

    private PdfResult buildPdfResult(PurchaseOrder order) {
        byte[] bytes = pdfService.generatePdf(order);

        if (bytes == null || bytes.length == 0) {
            log.error("❌ PDF generation returned empty");
            return PdfResult.empty();
        }

        String name = "PurchaseOrder-" + order.getPoNumber() + ".pdf";
        log.info("✅ PDF generated → {} bytes", bytes.length);
        return new PdfResult(bytes, name);
    }

    // ── Value object to hold PDF bytes + filename ─────────────────────────────

    private record PdfResult(byte[] bytes, String name) {

        static PdfResult empty() {
            return new PdfResult(null, null);
        }

        // Override to use Arrays.equals for byte[] content comparison
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PdfResult other)) return false;
            return java.util.Arrays.equals(bytes, other.bytes)
                    && java.util.Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return 31 * java.util.Arrays.hashCode(bytes)
                    + java.util.Objects.hashCode(name);
        }

        @Override
        public String toString() {
            return "PdfResult{name='" + name + "', bytes="
                    + (bytes != null ? bytes.length + " bytes" : "null") + "}";
        }
    }

    // ── Store Gmail ───────────────────────────────────────────────────────────

    private void sendViaStoreConfig(EmailDTO dto, byte[] pdfBytes, String pdfName) {
        StoreMailConfig config = mailConfigRepository
                .findByStoreIdAndActiveTrue(dto.getStoreId())
                .orElse(null);

        if (config == null) {
            log.warn("⚠️  No mail config for storeId={} — falling back to global",
                    dto.getStoreId());
            sendViaGlobalConfig(dto, pdfBytes, pdfName);
            return;
        }

        if (config.getFromEmail() == null || config.getFromEmail().isBlank()) {
            log.warn("⚠️  Store mail config for storeId={} has blank fromEmail — falling back to global",
                    dto.getStoreId());
            sendViaGlobalConfig(dto, pdfBytes, pdfName);
            return;
        }

        try {
            dynamicMailSender.sendWithPdf(
                    config,
                    dto.getTo(),
                    dto.getSubject(),
                    dto.getBody(),
                    pdfBytes,
                    pdfName);
            log.info("✅ Email sent via store Gmail → from={} to={} pdf={}",
                    config.getFromEmail(), dto.getTo(),
                    pdfName != null ? pdfName : "none");
        } catch (Exception e) {
            log.error("❌ Store Gmail send failed: {}", e.getMessage(), e);
            pushToDeadLetter(dto);
        }
    }

    // ── Global JavaMailSender ─────────────────────────────────────────────────

    private void sendViaGlobalConfig(EmailDTO dto, byte[] pdfBytes, String pdfName) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.error("❌ Global send skipped — app.mail.from is blank. Check environment config.");
            pushToDeadLetter(dto);
            return;
        }

        try {
            MimeMessage       message = mailSender.createMimeMessage();
            MimeMessageHelper helper  = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(dto.getTo());
            helper.setSubject(dto.getSubject());
            helper.setText(dto.getBody(), true);

            if (pdfBytes != null && pdfBytes.length > 0 && pdfName != null) {
                helper.addAttachment(pdfName,
                        new ByteArrayResource(pdfBytes),
                        "application/pdf");
                log.info("📎 PDF attached: {} ({} bytes)", pdfName, pdfBytes.length);
            }

            mailSender.send(message);
            log.info("✅ Email sent via global → to={} pdf={}",
                    dto.getTo(), pdfName != null ? pdfName : "none");

        } catch (Exception e) {
            log.error("❌ Global send failed: {}", e.getMessage(), e);
            pushToDeadLetter(dto);
        }
    }

    // ── Dead-letter queue — failed jobs are kept, not lost ────────────────────

    private void pushToDeadLetter(EmailDTO dto) {
        try {
            redisTemplate.opsForList().leftPush(EMAIL_QUEUE_FAILED, dto);
            log.warn("↪️  Job moved to {} → to={} subject={}",
                    EMAIL_QUEUE_FAILED, dto.getTo(), dto.getSubject());
        } catch (Exception e) {
            log.error("❌ Could not push to dead-letter queue: {}", e.getMessage(), e);
        }
    }
}