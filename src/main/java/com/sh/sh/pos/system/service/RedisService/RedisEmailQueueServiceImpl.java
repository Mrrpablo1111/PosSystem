package com.sh.sh.pos.system.service.redisservice;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.EmailType;
import com.sh.sh.pos.system.model.poemaillogs.POEmailLog;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.payload.dto.emaildto.EmailDTO;
import com.sh.sh.pos.system.repository.poemaillogrepository.POEmailLogRepository;
import com.sh.sh.pos.system.repository.purchasesorderrepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.service.emailservice.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisEmailQueueServiceImpl implements EmailService {

    private final RedisTemplate<String, EmailDTO> redisTemplate;
    private final POEmailLogRepository emailLogRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private static final String EMAIL_QUEUE = "email-queue";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── Shared HTML fragments (S1192) ─────────────────────────────────────

    private static final String HTML_HEAD_OPEN = "<!DOCTYPE html><html><head><meta charset='UTF-8'>";
    private static final String STYLE_BODY_BASE = "<style>body{font-family:Arial,sans-serif;color:#333;font-size:14px}";
    private static final String CSS_WRAP_600 = ".wrap{max-width:600px;margin:0 auto;padding:24px}";
    private static final String CSS_WRAP_680 = ".wrap{max-width:680px;margin:0 auto;padding:24px}";
    private static final String CSS_HDR_GREEN = ".hdr{background:#10b981;color:white;padding:20px 24px;border-radius:8px 8px 0 0}";
    private static final String CSS_BODY_PANEL = ".body{background:#f9fafb;padding:24px;border:1px solid #e5e7eb}";
    private static final String CSS_FOOTER_CLOSE_HEAD = ".footer{text-align:center;font-size:11px;color:#9ca3af;padding:16px}</style></head>";
    private static final String BODY_WRAP_OPEN = "<body><div class='wrap'>";
    private static final String DIV_BODY_OPEN = "<div class='body'>";
    private static final String TD_PADDING = "<tr><td style='padding:8px'>";
    private static final String TD_CLOSE_TD_PADDING = "</td><td style='padding:8px'>";
    private static final String TD_CLOSE = "</td>";
    private static final String TD_TR_CLOSE = "</td></tr>";
    private static final String STRONG_COMMA_P = "</strong>,</p>";
    private static final String FOOTER_EMAIL_FROM = "</div><div class='footer'>Automated email from ";
    private static final String CLOSE_HTML = ".</div></div></body></html>";

    @Value("${app.mail.from-name:SH-POS System}")
    private String fromName;

    public RedisEmailQueueServiceImpl(
            @Qualifier("emailRedisTemplate") RedisTemplate<String, EmailDTO> redisTemplate,
            POEmailLogRepository emailLogRepository,
            PurchaseOrderRepository purchaseOrderRepository) {
        this.redisTemplate = redisTemplate;
        this.emailLogRepository = emailLogRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    // ── Generic send ──────────────────────────────────────────────────────

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("sendEmail skipped — recipient is blank");
            return;
        }
        EmailDTO job = new EmailDTO(to, subject, body);
        redisTemplate.opsForList().leftPush(EMAIL_QUEUE, job);
        log.info("📩 Email queued → to={} subject={}", to, subject);
    }

    // ── PO email ──────────────────────────────────────────────────────────

    @Override
    public void sendPurchaseOrderEmail(PurchaseOrder order) {

        // Reload fresh from DB so all lazy relations are initialized
        PurchaseOrder freshOrder = purchaseOrderRepository
                .findById(order.getId())
                .orElse(order);

        log.info("🔴 sendPurchaseOrderEmail → PO={} id={} supplier={} storeId={}",
                freshOrder.getPoNumber(),
                freshOrder.getId(),
                freshOrder.getSupplier() != null ? freshOrder.getSupplier().getName() : "NULL",
                resolveStoreId(freshOrder));

        String to = resolveSupplierEmail(freshOrder);
        if (to == null) {
            return;
        }

        String subject = "Purchase Order " + freshOrder.getPoNumber() + " — " + storeName(freshOrder);
        String body = buildPoHtml(freshOrder);

        EmailDTO job = EmailDTO.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .storeId(resolveStoreId(freshOrder))
                .purchaseOrderId(freshOrder.getId())
                .build();

        log.info("📦 EmailDTO → to={} storeId={} purchaseOrderId={}",
                job.getTo(), job.getStoreId(), job.getPurchaseOrderId());

        try {
            redisTemplate.opsForList().leftPush(EMAIL_QUEUE, job);
            Long size = redisTemplate.opsForList().size(EMAIL_QUEUE);
            log.info("-> Pushed to Redis → queue size={}", size);
            saveLog(freshOrder, EmailType.PURCHASE_ORDER, to, subject, STATUS_SUCCESS, null);
        } catch (Exception e) {
            saveLog(freshOrder, EmailType.PURCHASE_ORDER, to, subject, STATUS_FAILED, e.getMessage());
            log.error("-> Failed to push: {}", e.getMessage(), e);
        }
    }

    // ── Goods received confirmation ───────────────────────────────────────

    @Override
    public void sendReceiptConfirmationEmail(PurchaseOrder order) {
        PurchaseOrder freshOrder = purchaseOrderRepository
                .findById(order.getId())
                .orElse(order);

        String to = resolveSupplierEmail(freshOrder);
        if (to == null) {
            return;
        }

        String subject = "Goods Received — " + freshOrder.getPoNumber() + " — " + storeName(freshOrder);
        String body = buildReceiptHtml(freshOrder);
        Long storeId = resolveStoreId(freshOrder);

        EmailDTO job = EmailDTO.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .storeId(storeId)
                .purchaseOrderId(freshOrder.getId())
                .build();

        try {
            redisTemplate.opsForList().leftPush(EMAIL_QUEUE, job);
            saveLog(freshOrder, EmailType.GOODS_RECEIVED, to, subject, STATUS_SUCCESS, null);
            log.info("📩 Receipt email queued → to={} storeId={} poId={}", to, storeId, freshOrder.getId());
        } catch (Exception e) {
            saveLog(freshOrder, EmailType.GOODS_RECEIVED, to, subject, STATUS_FAILED, e.getMessage());
            log.error("❌ Failed to queue receipt email: {}", e.getMessage(), e);
        }
    }

    // ── Welcome email ─────────────────────────────────────────────────────

    @Override
    public void sendWelcomeEmail(String to, String name, String tempPass) {
        String subject = "Welcome to " + fromName + " — Your account is ready";
        String body = HTML_HEAD_OPEN
                + STYLE_BODY_BASE
                + CSS_WRAP_600
                + CSS_HDR_GREEN
                + CSS_BODY_PANEL
                + ".pass{background:#fff;border:2px dashed #10b981;border-radius:8px;"
                + "padding:12px 20px;font-size:20px;font-weight:bold;color:#065f46;"
                + "letter-spacing:.1em;text-align:center;margin:16px 0}"
                + CSS_FOOTER_CLOSE_HEAD
                + BODY_WRAP_OPEN
                + "<div class='hdr'><h1 style='margin:0;font-size:22px'>Welcome to " + fromName + " 👋</h1></div>"
                + DIV_BODY_OPEN
                + "<p>Hi <strong>" + name + STRONG_COMMA_P
                + "<p>Your account has been created. Use the temporary password below to log in.</p>"
                + "<div class='pass'>" + tempPass + "</div>"
                + "<p style='color:#6b7280;font-size:13px'>Please change your password after first login.</p>"
                + FOOTER_EMAIL_FROM + fromName + CLOSE_HTML;
        sendEmail(to, subject, body);
        log.info("-> Welcome email queued → {}", to);
    }

    // ── Overdue PO alert ──────────────────────────────────────────────────

    @Override
    public void sendOverduePOAlert(PurchaseOrder order, String managerEmail) {
        if (managerEmail == null || managerEmail.isBlank()) {
            log.warn("No manager email — overdue alert skipped for PO {}", order.getPoNumber());
            return;
        }
        String subject = "-> Overdue PO — " + order.getPoNumber() + " — " + storeName(order);
        String expected = order.getExpectedDelivery() != null
                ? order.getExpectedDelivery().format(FMT)
                : "—";
        String supplierName = order.getSupplier() != null ? order.getSupplier().getName() : "—";
        String body = HTML_HEAD_OPEN
                + STYLE_BODY_BASE
                + CSS_WRAP_600
                + ".hdr{background:#f59e0b;color:white;padding:20px 24px;border-radius:8px 8px 0 0}"
                + CSS_BODY_PANEL
                + ".alert{background:#fffbeb;border:1px solid #fde68a;border-radius:8px;"
                + "padding:12px 16px;color:#92400e;font-size:13px;margin:12px 0}"
                + CSS_FOOTER_CLOSE_HEAD
                + BODY_WRAP_OPEN
                + "<div class='hdr'><h1 style='margin:0;font-size:20px'>⚠️ Overdue Purchase Order</h1></div>"
                + DIV_BODY_OPEN
                + "<p>The following purchase order is overdue:</p>"
                + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
                + TD_PADDING + "<strong>PO Number</strong></td><td>" + order.getPoNumber() + TD_TR_CLOSE
                + TD_PADDING + "<strong>Supplier</strong></td><td>" + supplierName + TD_TR_CLOSE
                + TD_PADDING + "<strong>Expected Delivery</strong></td><td>" + expected + TD_TR_CLOSE
                + TD_PADDING + "<strong>Status</strong></td><td>" + order.getStatus() + TD_TR_CLOSE
                + TD_PADDING + "<strong>Total Amount</strong></td><td>" + sym(order)
                + fmt(order.getTotalAmount()) + TD_TR_CLOSE
                + "</table>"
                + "<div class='alert'>Please contact the supplier and update or cancel the PO.</div>"
                + "</div><div class='footer'>Automated alert from " + fromName + CLOSE_HTML;
        sendEmail(managerEmail, subject, body);
        saveLog(order, EmailType.OVERDUE_PO_ALERT, managerEmail, subject, STATUS_SUCCESS, null);
        log.info("-> Overdue alert queued → {} for PO {}", managerEmail, order.getPoNumber());
    }

    // ── Daily sales report ────────────────────────────────────────────────

    @Override
    public void sendDailySalesReport(Store store, String reportHtml) {
        String email = storeContactEmail(store);
        if (email == null || email.isBlank()) {
            log.warn("No contact email for store {}", store.getBrand());
            return;
        }
        log.info("-> Daily sales report queued → {}", email);
        sendEmail(email, "📊 Daily Sales Report — " + store.getBrand(), reportHtml);
    }

    // ── Low stock alert ───────────────────────────────────────────────────

    @Override
    public void sendLowStockAlert(Store store, List<ProductBatch> lowBatches, String managerEmail) {
        if (managerEmail == null || managerEmail.isBlank() || lowBatches.isEmpty()) {
            return;
        }
        StringBuilder rows = new StringBuilder();
        for (ProductBatch b : lowBatches) {
            rows.append(TD_PADDING)
                    .append(b.getProduct() != null ? b.getProduct().getName() : "—")
                    .append(TD_CLOSE_TD_PADDING).append(b.getBatchNo())
                    .append("</td><td style='padding:8px;color:#dc2626;font-weight:bold'>")
                    .append(b.getRemainingQuantity()).append(TD_TR_CLOSE);
        }
        log.info("-> Low stock alert queued → {}", managerEmail);
        String body = "<html><body style='font-family:Arial'><h2 style='color:#ef4444'>⚠️ Low Stock — "
                + store.getBrand() + "</h2>"
                + "<table style='width:100%;border-collapse:collapse'><thead><tr><th style='text-align:left;padding:8px;background:#fee2e2'>Product</th>"
                + "<th style='text-align:left;padding:8px;background:#fee2e2'>Batch</th>"
                + "<th style='text-align:left;padding:8px;background:#fee2e2'>Remaining</th></tr></thead><tbody>"
                + rows + "</tbody></table></body></html>";
        sendEmail(managerEmail, "⚠️ Low Stock Alert — " + store.getBrand(), body);
    }

    // ── Expiry alert ──────────────────────────────────────────────────────

    @Override
    public void sendExpiryAlert(Store store, List<ProductBatch> expiringBatches, String managerEmail) {
        if (managerEmail == null || managerEmail.isBlank() || expiringBatches.isEmpty()) {
            return;
        }
        StringBuilder rows = new StringBuilder();
        for (ProductBatch b : expiringBatches) {
            rows.append(TD_PADDING)
                    .append(b.getProduct() != null ? b.getProduct().getName() : "—")
                    .append(TD_CLOSE_TD_PADDING).append(b.getBatchNo())
                    .append(TD_CLOSE_TD_PADDING).append(b.getRemainingQuantity())
                    .append("</td><td style='padding:8px;color:#d97706;font-weight:bold'>")
                    .append(b.getExpiryDate() != null ? b.getExpiryDate().format(FMT) : "—")
                    .append(TD_TR_CLOSE);
        }
        String body = "<html><body style='font-family:Arial'><h2 style='color:#f59e0b'>⏰ Expiry Alert — "
                + store.getBrand() + "</h2>"
                + "<table style='width:100%;border-collapse:collapse'><thead><tr>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Product</th>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Batch</th>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Qty</th>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Expiry</th></tr></thead><tbody>"
                + rows + "</tbody></table></body></html>";
        sendEmail(managerEmail, "-> Expiry Alert — " + store.getBrand(), body);
    }

    // ── Monthly report ────────────────────────────────────────────────────

    @Override
    public void sendMonthlyReport(Store store, String reportHtml) {
        String email = storeContactEmail(store);
        if (email == null || email.isBlank()) {
            log.warn("No contact email for store {}", store.getBrand());
            return;
        }
        LocalDate last = LocalDate.now().minusMonths(1);
        String subject = "📅 Monthly Report — " + store.getBrand() + " — "
                + last.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + last.getYear();
        sendEmail(email, subject, reportHtml);
    }

    // ── Email log ─────────────────────────────────────────────────────────

    private void saveLog(PurchaseOrder order, EmailType type,
            String to, String subject, String status, String error) {
        try {
            emailLogRepository.save(POEmailLog.builder()
                    .purchaseOrder(order)
                    .emailType(type)
                    .sentTo(to)
                    .subject(subject)
                    .status(status)
                    .errorMessage(error)
                    .build());
        } catch (Exception e) {
            log.warn("Could not save email log: {}", e.getMessage());
        }
    }

    // ── HTML builders ─────────────────────────────────────────────────────

    private String buildPoHtml(PurchaseOrder order) {
        String sym = sym(order);
        String name = storeName(order);
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_HEAD_OPEN)
                .append("<style>body{font-family:Arial,sans-serif;color:#333;font-size:14px;margin:0}")
                .append(CSS_WRAP_680)
                .append(CSS_HDR_GREEN)
                .append(".hdr h1{margin:0;font-size:22px}.hdr p{margin:4px 0 0;font-size:13px;opacity:.85}")
                .append(CSS_BODY_PANEL)
                .append("table{width:100%;border-collapse:collapse;margin:16px 0}")
                .append("th{background:#10b981;color:white;padding:8px 10px;text-align:left;font-size:12px}")
                .append("td{padding:8px 10px;border-bottom:1px solid #e5e7eb;font-size:13px}")
                .append(".tr-sub td{font-weight:bold;background:#f3f4f6}")
                .append(".tr-grand td{font-weight:bold;color:#10b981;font-size:15px;background:#ecfdf5}")
                .append(".notes{background:#fff;border:1px solid #e5e7eb;border-radius:6px;padding:12px;margin-top:16px}")
                .append(CSS_FOOTER_CLOSE_HEAD)
                .append(BODY_WRAP_OPEN);
        sb.append("<div class='hdr'><h1>Purchase Order</h1><p>").append(name).append("</p></div>")
                .append(DIV_BODY_OPEN)
                .append("<p>Dear <strong>").append(order.getSupplier().getName()).append(STRONG_COMMA_P)
                .append("<p>Please find below our purchase order. A PDF copy is attached for your records.</p>");
        sb.append("<table><tr>")
                .append("<td><strong>PO Number</strong><br>").append(order.getPoNumber()).append(TD_CLOSE)
                .append("<td><strong>Order Date</strong><br>").append(order.getOrderDate().format(FMT))
                .append(TD_CLOSE);
        if (order.getExpectedDelivery() != null) {
            sb.append("<td><strong>Expected Delivery</strong><br>").append(order.getExpectedDelivery().format(FMT))
                    .append(TD_CLOSE);
        }
        if (order.getPaymentDueDate() != null) {
            sb.append("<td><strong>Payment Due</strong><br>").append(order.getPaymentDueDate().format(FMT))
                    .append(TD_CLOSE);
        }
        sb.append("</tr></table>");
        sb.append(
                "<table><thead><tr><th>#</th><th>Product</th><th>Variant</th><th>Qty</th><th>Unit cost</th><th>Total</th></tr></thead><tbody>");
        int i = 1;
        for (PurchaseOrderItem item : order.getItems()) {
            String p = item.getProduct() != null ? item.getProduct().getName() : "—";
            String v = item.getVariant() != null ? item.getVariant().getName() : "—";
            sb.append("<tr><td>").append(i++).append(TD_CLOSE)
                    .append("<td>").append(p).append("</td><td>").append(v).append(TD_CLOSE)
                    .append("<td>").append(item.getRequestedQuantity()).append(TD_CLOSE)
                    .append("<td>").append(sym).append(fmt(item.getUnitCost())).append(TD_CLOSE)
                    .append("<td>").append(sym).append(fmt(item.getTotalCost())).append(TD_TR_CLOSE);
        }
        sb.append("</tbody></table>");
        sb.append("<table>")
                .append("<tr class='tr-sub'><td>Subtotal</td><td>").append(sym).append(fmt(order.getSubtotal()))
                .append(TD_TR_CLOSE);
        if (gt0(order.getDiscount())) {
            sb.append("<tr class='tr-sub'><td>Discount</td><td>- ").append(sym).append(fmt(order.getDiscount()))
                    .append(TD_TR_CLOSE);
        }
        if (gt0(order.getTax())) {
            sb.append("<tr class='tr-sub'><td>Tax</td><td>+ ").append(sym).append(fmt(order.getTax()))
                    .append(TD_TR_CLOSE);
        }
        if (gt0(order.getShippingCost())) {
            sb.append("<tr class='tr-sub'><td>Shipping</td><td>+ ").append(sym).append(fmt(order.getShippingCost()))
                    .append(TD_TR_CLOSE);
        }
        sb.append("<tr class='tr-grand'><td>Grand Total</td><td>").append(sym).append(fmt(order.getTotalAmount()))
                .append("</td></tr></table>");
        if (order.getNotes() != null && !order.getNotes().isBlank()) {
            sb.append("<div class='notes'><strong>Notes:</strong><br>").append(order.getNotes()).append("</div>");
        }
        if (order.getCarrierId() != null) {
            sb.append("<p style='margin-top:16px;font-size:13px;color:#6b7280'>Carrier: <strong>")
                    .append(order.getCarrierId()).append("</strong>");
            if (order.getTrackingNo() != null) {
                sb.append(" &nbsp;|&nbsp; Tracking: <strong>").append(order.getTrackingNo()).append("</strong>");
            }
            sb.append("</p>");
        }
        sb.append(FOOTER_EMAIL_FROM).append(name)
                .append(". Please do not reply directly.</div></div></body></html>");
        return sb.toString();
    }

    private String buildReceiptHtml(PurchaseOrder order) {
        String sym = sym(order);
        String name = storeName(order);
        String date = order.getReceivedDate() != null ? order.getReceivedDate().format(FMT) : "—";
        return HTML_HEAD_OPEN
                + STYLE_BODY_BASE
                + CSS_WRAP_680
                + CSS_HDR_GREEN
                + CSS_BODY_PANEL
                + ".badge{background:#ecfdf5;color:#065f46;border:1px solid #6ee7b7;border-radius:20px;padding:4px 14px;font-weight:bold;font-size:13px}"
                + CSS_FOOTER_CLOSE_HEAD
                + BODY_WRAP_OPEN
                + "<div class='hdr'><h1 style='margin:0;font-size:22px'>Goods Received ✓</h1>"
                + "<p style='margin:4px 0 0;opacity:.85;font-size:13px'>" + name + "</p></div>"
                + DIV_BODY_OPEN
                + "<p>Dear <strong>" + order.getSupplier().getName() + STRONG_COMMA_P
                + "<p>We confirm that goods for <strong>" + order.getPoNumber() + "</strong> have been received.</p>"
                + "<table><tr><td><strong>PO Number</strong><br>" + order.getPoNumber() + TD_CLOSE
                + "<td><strong>Received Date</strong><br>" + date + TD_CLOSE
                + "<td><strong>Total Amount</strong><br>" + sym + fmt(order.getTotalAmount()) + "</td></tr></table>"
                + "<p><span class='badge'>✓ All items received</span></p>"
                + "<p style='margin-top:20px;color:#6b7280;font-size:13px'>Thank you for your prompt delivery.</p>"
                + FOOTER_EMAIL_FROM + name + CLOSE_HTML;
    }

    // ── Util ──────────────────────────────────────────────────────────────

    private String resolveSupplierEmail(PurchaseOrder order) {
        if (order.getSupplier() == null) {
            log.warn("-> PO {} has no supplier", order.getPoNumber());
            return null;
        }
        String email = order.getSupplier().getEmail();
        log.info("🔍 Supplier: name={} email={}", order.getSupplier().getName(), email);
        if (email == null || email.isBlank()) {
            log.warn("-> Supplier '{}' has no email — skipped", order.getSupplier().getName());
            return null;
        }
        return email;
    }

    private Long resolveStoreId(PurchaseOrder order) {
        return Optional.ofNullable(order.getBranch())
                .map(branch -> branch.getStore())
                .map(store -> store.getId())
                .orElse(null);
    }

    private String storeName(PurchaseOrder order) {
        return Optional.ofNullable(order.getBranch())
                .map(branch -> branch.getStore())
                .map(store -> store.getBrand())
                .orElse(fromName);
    }

    private String storeContactEmail(Store store) {
        return Optional.ofNullable(store)
                .map(Store::getContact)
                .map(contact -> contact.getEmail())
                .orElse(null);
    }

    private String sym(PurchaseOrder order) {
        return order.getCurrency() != null ? order.getCurrency().getSymbol() : "$";
    }

    private String fmt(BigDecimal v) {
        return v != null ? String.format("%.2f", v) : "0.00";
    }

    private boolean gt0(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public void sendOutstandingItemsReminderEmail(PurchaseOrder order) {
        if (order.getSupplier() == null || order.getSupplier().getEmail() == null
                || order.getSupplier().getEmail().isBlank()) {
            log.warn("Cannot send outstanding-items reminder for PO {}: supplier has no email",
                    order.getPoNumber());
            return;
        }

        StringBuilder rows = new StringBuilder();
        int outstandingLines = 0;

        for (var item : order.getItems()) {
            int ordered = item.getRequestedQuantity() != null ? item.getRequestedQuantity() : 0;
            int received = item.getReceivedQuantity() != null ? item.getReceivedQuantity() : 0;
            int remaining = ordered - received;
            if (remaining <= 0)
                continue;

            outstandingLines++;
            String productName = item.getProduct() != null ? item.getProduct().getName() : "—";
            String variantName = item.getVariant() != null ? item.getVariant().getName() : "";

            rows.append("<tr>")
                    .append("<td style='padding:8px;border-bottom:1px solid #eee;'>")
                    .append(productName)
                    .append(variantName.isBlank() ? "" : " <span style='color:#888;'>(" + variantName + ")</span>")
                    .append("</td>")
                    .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:center;'>").append(ordered)
                    .append("</td>")
                    .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:center;'>").append(received)
                    .append("</td>")
                    .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:center;font-weight:bold;color:#c0392b;'>")
                    .append(remaining)
                    .append("</td>")
                    .append("</tr>");
        }

        if (outstandingLines == 0) {
            log.info("No outstanding items on PO {} — reminder not sent", order.getPoNumber());
            return;
        }

        String storeName = (order.getBranch() != null && order.getBranch().getStore() != null)
                ? order.getBranch().getStore().getBrand()
                : "Purchasing Team";

        String html = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>
                  <h2 style='color:#2c3e50;'>Outstanding Items Reminder</h2>
                  <p>Dear %s,</p>
                  <p>This is a reminder that the following items from purchase order
                     <strong>%s</strong> are still outstanding. Please arrange delivery
                     of the remaining quantities at your earliest convenience.</p>
                  <table style='width:100%%;border-collapse:collapse;margin-top:16px;'>
                    <thead>
                      <tr style='background:#f5f5f5;'>
                        <th style='padding:8px;text-align:left;'>Product</th>
                        <th style='padding:8px;text-align:center;'>Ordered</th>
                        <th style='padding:8px;text-align:center;'>Received</th>
                        <th style='padding:8px;text-align:center;'>Remaining</th>
                      </tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                  <p style='margin-top:16px;color:#555;'>Thank you,<br/>%s</p>
                </div>
                """.formatted(order.getSupplier().getName(), order.getPoNumber(), rows.toString(), storeName);

        sendEmail(order.getSupplier().getEmail(),
                "Outstanding Items Reminder — " + order.getPoNumber(), html);

        log.info("Outstanding-items reminder sent for PO {} ({} lines)",
                order.getPoNumber(), outstandingLines);
    }
}