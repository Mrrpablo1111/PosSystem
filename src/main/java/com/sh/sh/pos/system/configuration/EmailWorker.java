package com.sh.sh.pos.system.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;
import com.sh.sh.pos.system.model.storeMailConfigs.StoreMailConfig;
import com.sh.sh.pos.system.payload.dto.EmailDTO;

import com.sh.sh.pos.system.repository.purchasesOrderRepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.repository.storeMailConfigsRepository.StoreMailConfigRepository;
import com.sh.sh.pos.system.service.emailService.DynamicMailSender;
import com.sh.sh.pos.system.service.purchaseOrdersPdfService.PurchaseOrderPdfService;

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

    @Scheduled(fixedDelay = 3000)
    public void consumeQueue() {
        try {
            EmailDTO dto = redisTemplate.opsForList().rightPop(EMAIL_QUEUE);
            if (dto == null) return;

            log.info("📬 Dequeued → to={} storeId={} purchaseOrderId={}",
                    dto.getTo(), dto.getStoreId(), dto.getPurchaseOrderId());

            if (dto.getTo() == null || dto.getTo().isBlank()) {
                log.warn("⚠️  Skipped — to is blank");
                return;
            }

            // ── Generate PDF ──────────────────────────────────────────────
            byte[] pdfBytes = null;
            String pdfName  = null;

            if (dto.getPurchaseOrderId() != null) {
                try {
                    // FIX: use findByIdWithAllRelations — loads items, supplier,
                    // branch, store, currency eagerly so no LazyInitializationException
                    PurchaseOrder order = purchaseOrderRepository
                            .findByIdWithAllRelations(dto.getPurchaseOrderId())
                            .orElse(null);

                    if (order == null) {
                        log.error("❌ PO not found: id={}", dto.getPurchaseOrderId());
                    } else {
                        log.info("✅ PO loaded → poNumber={} items={}",
                                order.getPoNumber(),
                                order.getItems() != null ? order.getItems().size() : 0);

                        pdfBytes = pdfService.generatePdf(order);

                        if (pdfBytes != null && pdfBytes.length > 0) {
                            pdfName = "PurchaseOrder-" + order.getPoNumber() + ".pdf";
                            log.info("✅ PDF generated → {} bytes", pdfBytes.length);
                        } else {
                            log.error("❌ PDF generation returned empty");
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ PDF generation failed: {}", e.getMessage(), e);
                }
            }

            // ── Send ──────────────────────────────────────────────────────
            if (dto.getStoreId() != null) {
                sendViaStoreConfig(dto, pdfBytes, pdfName);
            } else {
                sendViaGlobalConfig(dto, pdfBytes, pdfName);
            }

        } catch (Exception e) {
            log.error("❌ consumeQueue error: {}", e.getMessage(), e);
        }
    }

    // ── Store Gmail ───────────────────────────────────────────────────────

    private void sendViaStoreConfig(EmailDTO dto, byte[] pdfBytes, String pdfName) {
        StoreMailConfig config = mailConfigRepository
                .findByStoreIdAndActiveTrue(dto.getStoreId())
                .orElse(null);

        if (config == null) {
            log.warn("⚠️  No mail config for storeId={} — falling back to global", dto.getStoreId());
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
        }
    }

    // ── Global JavaMailSender ─────────────────────────────────────────────

    private void sendViaGlobalConfig(EmailDTO dto, byte[] pdfBytes, String pdfName) {
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
        }
    }
}