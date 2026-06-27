package com.sh.sh.pos.system.service.RedisService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.EmailType;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.poEmailLogs.POEmailLog;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesOrderItem.PurchaseOrderItem;
import com.sh.sh.pos.system.payload.dto.EmailDTO;
import com.sh.sh.pos.system.repository.poEmailLogRepository.POEmailLogRepository;
import com.sh.sh.pos.system.repository.purchasesOrderRepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.service.EmailService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisEmailQueueServiceImpl implements EmailService {

    private final RedisTemplate<String, EmailDTO> redisTemplate;
    private final POEmailLogRepository            emailLogRepository;
    private final PurchaseOrderRepository         purchaseOrderRepository; 

    @Value("${app.mail.from-name:SH-POS System}")
    private String fromName;

    private static final String EMAIL_QUEUE = "email-queue";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public RedisEmailQueueServiceImpl(
            @Qualifier("emailRedisTemplate") RedisTemplate<String, EmailDTO> redisTemplate,
            POEmailLogRepository emailLogRepository,
            PurchaseOrderRepository purchaseOrderRepository) { // ← NEW
        this.redisTemplate           = redisTemplate;
        this.emailLogRepository      = emailLogRepository;
        this.purchaseOrderRepository = purchaseOrderRepository; // ← NEW
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

        // FIX: reload fresh from DB so all lazy relations are initialized
        // Without this, branch.store and supplier.email may not be loaded
        // because the caller is inside a @Transactional scope
        PurchaseOrder freshOrder = purchaseOrderRepository
                .findById(order.getId())
                .orElse(order);

        log.info("🔴 sendPurchaseOrderEmail → PO={} id={} supplier={} storeId={}",
                freshOrder.getPoNumber(),
                freshOrder.getId(),
                freshOrder.getSupplier() != null ? freshOrder.getSupplier().getName() : "NULL",
                resolveStoreId(freshOrder));

        String to = resolveSupplierEmail(freshOrder);
        if (to == null) return;

        String subject = "Purchase Order " + freshOrder.getPoNumber() + " — " + storeName(freshOrder);
        String body    = buildPoHtml(freshOrder);

        Long storeId         = resolveStoreId(freshOrder);
        Long purchaseOrderId = freshOrder.getId();

        EmailDTO job = EmailDTO.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .storeId(storeId)
                .purchaseOrderId(purchaseOrderId)
                .build();

        log.info("📦 EmailDTO → to={} storeId={} purchaseOrderId={}",
                job.getTo(), job.getStoreId(), job.getPurchaseOrderId());

        try {
            redisTemplate.opsForList().leftPush(EMAIL_QUEUE, job);
            Long size = redisTemplate.opsForList().size(EMAIL_QUEUE);
            log.info("✅ Pushed to Redis → queue size={}", size);
            saveLog(freshOrder, EmailType.PURCHASE_ORDER, to, subject, "SUCCESS", null);
        } catch (Exception e) {
            saveLog(freshOrder, EmailType.PURCHASE_ORDER, to, subject, "FAILED", e.getMessage());
            log.error("❌ Failed to push: {}", e.getMessage(), e);
        }
    }

    // ── Goods received confirmation ───────────────────────────────────────

    @Override
    public void sendReceiptConfirmationEmail(PurchaseOrder order) {
        PurchaseOrder freshOrder = purchaseOrderRepository
                .findById(order.getId())
                .orElse(order);

        String to = resolveSupplierEmail(freshOrder);
        if (to == null) return;

        String subject = "Goods Received — " + freshOrder.getPoNumber() + " — " + storeName(freshOrder);
        String body    = buildReceiptHtml(freshOrder);
        Long storeId   = resolveStoreId(freshOrder);

        EmailDTO job = EmailDTO.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .storeId(storeId)
                .purchaseOrderId(freshOrder.getId())
                .build();

        try {
            redisTemplate.opsForList().leftPush(EMAIL_QUEUE, job);
            saveLog(freshOrder, EmailType.GOODS_RECEIVED, to, subject, "SUCCESS", null);
            log.info("📩 Receipt email queued → to={} storeId={}", to, storeId, freshOrder.getId());
        } catch (Exception e) {
            saveLog(freshOrder, EmailType.GOODS_RECEIVED, to, subject, "FAILED", e.getMessage());
            log.error("❌ Failed to queue receipt email: {}", e.getMessage(), e);
        }
    }

    // ── Welcome email ─────────────────────────────────────────────────────

    @Override
    public void sendWelcomeEmail(String to, String name, String tempPass) {
        String subject = "Welcome to " + fromName + " — Your account is ready";
        String body =
                "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<style>body{font-family:Arial,sans-serif;color:#333;font-size:14px}"
                + ".wrap{max-width:600px;margin:0 auto;padding:24px}"
                + ".hdr{background:#10b981;color:white;padding:20px 24px;border-radius:8px 8px 0 0}"
                + ".body{background:#f9fafb;padding:24px;border:1px solid #e5e7eb}"
                + ".pass{background:#fff;border:2px dashed #10b981;border-radius:8px;"
                + "padding:12px 20px;font-size:20px;font-weight:bold;color:#065f46;"
                + "letter-spacing:.1em;text-align:center;margin:16px 0}"
                + ".footer{text-align:center;font-size:11px;color:#9ca3af;padding:16px}</style></head>"
                + "<body><div class='wrap'>"
                + "<div class='hdr'><h1 style='margin:0;font-size:22px'>Welcome to " + fromName + " 👋</h1></div>"
                + "<div class='body'>"
                + "<p>Hi <strong>" + name + "</strong>,</p>"
                + "<p>Your account has been created. Use the temporary password below to log in.</p>"
                + "<div class='pass'>" + tempPass + "</div>"
                + "<p style='color:#6b7280;font-size:13px'>Please change your password after first login.</p>"
                + "</div><div class='footer'>Automated email from " + fromName + ".</div></div></body></html>";
        sendEmail(to, subject, body);
        log.info("📩 Welcome email queued → {}", to);
    }

    // ── Overdue PO alert ──────────────────────────────────────────────────

    @Override
    public void sendOverduePOAlert(PurchaseOrder order, String managerEmail) {
        if (managerEmail == null || managerEmail.isBlank()) {
            log.warn("No manager email — overdue alert skipped for PO {}", order.getPoNumber());
            return;
        }
        String subject  = "⚠️ Overdue PO — " + order.getPoNumber() + " — " + storeName(order);
        String expected = order.getExpectedDelivery() != null
                ? order.getExpectedDelivery().format(FMT) : "—";
        String body =
                "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<style>body{font-family:Arial,sans-serif;color:#333;font-size:14px}"
                + ".wrap{max-width:600px;margin:0 auto;padding:24px}"
                + ".hdr{background:#f59e0b;color:white;padding:20px 24px;border-radius:8px 8px 0 0}"
                + ".body{background:#f9fafb;padding:24px;border:1px solid #e5e7eb}"
                + ".alert{background:#fffbeb;border:1px solid #fde68a;border-radius:8px;"
                + "padding:12px 16px;color:#92400e;font-size:13px;margin:12px 0}"
                + ".footer{text-align:center;font-size:11px;color:#9ca3af;padding:16px}</style></head>"
                + "<body><div class='wrap'>"
                + "<div class='hdr'><h1 style='margin:0;font-size:20px'>⚠️ Overdue Purchase Order</h1></div>"
                + "<div class='body'>"
                + "<p>The following purchase order is overdue:</p>"
                + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
                + "<tr><td style='padding:8px'><strong>PO Number</strong></td><td>" + order.getPoNumber() + "</td></tr>"
                + "<tr><td style='padding:8px'><strong>Supplier</strong></td><td>" + order.getSupplier().getName() + "</td></tr>"
                + "<tr><td style='padding:8px'><strong>Expected Delivery</strong></td><td>" + expected + "</td></tr>"
                + "<tr><td style='padding:8px'><strong>Status</strong></td><td>" + order.getStatus() + "</td></tr>"
                + "<tr><td style='padding:8px'><strong>Total Amount</strong></td><td>" + sym(order) + fmt(order.getTotalAmount()) + "</td></tr>"
                + "</table>"
                + "<div class='alert'>Please contact the supplier and update or cancel the PO.</div>"
                + "</div><div class='footer'>Automated alert from " + fromName + ".</div></div></body></html>";
        sendEmail(managerEmail, subject, body);
        saveLog(order, EmailType.OVERDUE_PO_ALERT, managerEmail, subject, "SUCCESS", null);
        log.info("📩 Overdue alert queued → {} for PO {}", managerEmail, order.getPoNumber());
    }

    // ── Daily sales report ────────────────────────────────────────────────

    @Override
    public void sendDailySalesReport(Store store, String reportHtml) {
        String email = store.getContact() != null ? store.getContact().getEmail() : null;
        if (email == null || email.isBlank()) { log.warn("No contact email for store {}", store.getBrand()); return; }
        sendEmail(email, "📊 Daily Sales Report — " + store.getBrand(), reportHtml);
    }

    // ── Low stock alert ───────────────────────────────────────────────────

    @Override
    public void sendLowStockAlert(Store store,
            List<com.sh.sh.pos.system.model.products.ProductBatch> lowBatches,
            String managerEmail) {
        if (managerEmail == null || managerEmail.isBlank() || lowBatches.isEmpty()) return;
        StringBuilder rows = new StringBuilder();
        for (var b : lowBatches) {
            rows.append("<tr><td style='padding:8px'>")
                .append(b.getProduct() != null ? b.getProduct().getName() : "—")
                .append("</td><td style='padding:8px'>").append(b.getBatchNo())
                .append("</td><td style='padding:8px;color:#dc2626;font-weight:bold'>")
                .append(b.getRemainingQuantity()).append("</td></tr>");
        }
        String body = "<html><body style='font-family:Arial'><h2 style='color:#ef4444'>⚠️ Low Stock — " + store.getBrand() + "</h2>"
                + "<table style='width:100%;border-collapse:collapse'><thead><tr><th style='text-align:left;padding:8px;background:#fee2e2'>Product</th>"
                + "<th style='text-align:left;padding:8px;background:#fee2e2'>Batch</th>"
                + "<th style='text-align:left;padding:8px;background:#fee2e2'>Remaining</th></tr></thead><tbody>"
                + rows + "</tbody></table></body></html>";
        sendEmail(managerEmail, "⚠️ Low Stock Alert — " + store.getBrand(), body);
    }

    // ── Expiry alert ──────────────────────────────────────────────────────

    @Override
    public void sendExpiryAlert(Store store,
            List<com.sh.sh.pos.system.model.products.ProductBatch> expiringBatches,
            String managerEmail) {
        if (managerEmail == null || managerEmail.isBlank() || expiringBatches.isEmpty()) return;
        StringBuilder rows = new StringBuilder();
        for (var b : expiringBatches) {
            rows.append("<tr><td style='padding:8px'>")
                .append(b.getProduct() != null ? b.getProduct().getName() : "—")
                .append("</td><td style='padding:8px'>").append(b.getBatchNo())
                .append("</td><td style='padding:8px'>").append(b.getRemainingQuantity())
                .append("</td><td style='padding:8px;color:#d97706;font-weight:bold'>")
                .append(b.getExpiryDate() != null ? b.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "—")
                .append("</td></tr>");
        }
        String body = "<html><body style='font-family:Arial'><h2 style='color:#f59e0b'>⏰ Expiry Alert — " + store.getBrand() + "</h2>"
                + "<table style='width:100%;border-collapse:collapse'><thead><tr>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Product</th>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Batch</th>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Qty</th>"
                + "<th style='text-align:left;padding:8px;background:#fef3c7'>Expiry</th></tr></thead><tbody>"
                + rows + "</tbody></table></body></html>";
        sendEmail(managerEmail, "⏰ Expiry Alert — " + store.getBrand(), body);
    }

    // ── Monthly report ────────────────────────────────────────────────────

    @Override
    public void sendMonthlyReport(Store store, String reportHtml) {
        String email = store.getContact() != null ? store.getContact().getEmail() : null;
        if (email == null || email.isBlank()) { log.warn("No contact email for store {}", store.getBrand()); return; }
        java.time.LocalDate last = java.time.LocalDate.now().minusMonths(1);
        String subject = "📅 Monthly Report — " + store.getBrand() + " — "
                + last.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
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
        String sym  = sym(order);
        String name = storeName(order);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
          .append("<style>body{font-family:Arial,sans-serif;color:#333;font-size:14px;margin:0}")
          .append(".wrap{max-width:680px;margin:0 auto;padding:24px}")
          .append(".hdr{background:#10b981;color:white;padding:20px 24px;border-radius:8px 8px 0 0}")
          .append(".hdr h1{margin:0;font-size:22px}.hdr p{margin:4px 0 0;font-size:13px;opacity:.85}")
          .append(".body{background:#f9fafb;padding:24px;border:1px solid #e5e7eb}")
          .append("table{width:100%;border-collapse:collapse;margin:16px 0}")
          .append("th{background:#10b981;color:white;padding:8px 10px;text-align:left;font-size:12px}")
          .append("td{padding:8px 10px;border-bottom:1px solid #e5e7eb;font-size:13px}")
          .append(".tr-sub td{font-weight:bold;background:#f3f4f6}")
          .append(".tr-grand td{font-weight:bold;color:#10b981;font-size:15px;background:#ecfdf5}")
          .append(".notes{background:#fff;border:1px solid #e5e7eb;border-radius:6px;padding:12px;margin-top:16px}")
          .append(".footer{text-align:center;font-size:11px;color:#9ca3af;padding:16px}")
          .append("</style></head><body><div class='wrap'>");
        sb.append("<div class='hdr'><h1>Purchase Order</h1><p>").append(name).append("</p></div>")
          .append("<div class='body'>")
          .append("<p>Dear <strong>").append(order.getSupplier().getName()).append("</strong>,</p>")
          .append("<p>Please find below our purchase order. A PDF copy is attached for your records.</p>");
        sb.append("<table><tr>")
          .append("<td><strong>PO Number</strong><br>").append(order.getPoNumber()).append("</td>")
          .append("<td><strong>Order Date</strong><br>").append(order.getOrderDate().format(FMT)).append("</td>");
        if (order.getExpectedDelivery() != null)
            sb.append("<td><strong>Expected Delivery</strong><br>").append(order.getExpectedDelivery().format(FMT)).append("</td>");
        if (order.getPaymentDueDate() != null)
            sb.append("<td><strong>Payment Due</strong><br>").append(order.getPaymentDueDate().format(FMT)).append("</td>");
        sb.append("</tr></table>");
        sb.append("<table><thead><tr><th>#</th><th>Product</th><th>Variant</th><th>Qty</th><th>Unit cost</th><th>Total</th></tr></thead><tbody>");
        int i = 1;
        for (PurchaseOrderItem item : order.getItems()) {
            String p = item.getProduct() != null ? item.getProduct().getName() : "—";
            String v = item.getVariant()  != null ? item.getVariant().getName()  : "—";
            sb.append("<tr><td>").append(i++).append("</td>")
              .append("<td>").append(p).append("</td><td>").append(v).append("</td>")
              .append("<td>").append(item.getRequestedQuantity()).append("</td>")
              .append("<td>").append(sym).append(fmt(item.getUnitCost())).append("</td>")
              .append("<td>").append(sym).append(fmt(item.getTotalCost())).append("</td></tr>");
        }
        sb.append("</tbody></table>");
        sb.append("<table>")
          .append("<tr class='tr-sub'><td>Subtotal</td><td>").append(sym).append(fmt(order.getSubtotal())).append("</td></tr>");
        if (gt0(order.getDiscount()))
            sb.append("<tr class='tr-sub'><td>Discount</td><td>- ").append(sym).append(fmt(order.getDiscount())).append("</td></tr>");
        if (gt0(order.getTax()))
            sb.append("<tr class='tr-sub'><td>Tax</td><td>+ ").append(sym).append(fmt(order.getTax())).append("</td></tr>");
        if (gt0(order.getShippingCost()))
            sb.append("<tr class='tr-sub'><td>Shipping</td><td>+ ").append(sym).append(fmt(order.getShippingCost())).append("</td></tr>");
        sb.append("<tr class='tr-grand'><td>Grand Total</td><td>").append(sym).append(fmt(order.getTotalAmount())).append("</td></tr></table>");
        if (order.getNotes() != null && !order.getNotes().isBlank())
            sb.append("<div class='notes'><strong>Notes:</strong><br>").append(order.getNotes()).append("</div>");
        if (order.getCarrierId() != null) {
            sb.append("<p style='margin-top:16px;font-size:13px;color:#6b7280'>Carrier: <strong>").append(order.getCarrierId()).append("</strong>");
            if (order.getTrackingNo() != null)
                sb.append(" &nbsp;|&nbsp; Tracking: <strong>").append(order.getTrackingNo()).append("</strong>");
            sb.append("</p>");
        }
        sb.append("</div><div class='footer'>Automated email from ").append(name).append(". Please do not reply directly.</div></div></body></html>");
        return sb.toString();
    }

    private String buildReceiptHtml(PurchaseOrder order) {
        String sym  = sym(order);
        String name = storeName(order);
        String date = order.getReceivedDate() != null ? order.getReceivedDate().format(FMT) : "—";
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
             + "<style>body{font-family:Arial,sans-serif;color:#333;font-size:14px}"
             + ".wrap{max-width:680px;margin:0 auto;padding:24px}"
             + ".hdr{background:#10b981;color:white;padding:20px 24px;border-radius:8px 8px 0 0}"
             + ".body{background:#f9fafb;padding:24px;border:1px solid #e5e7eb}"
             + ".badge{background:#ecfdf5;color:#065f46;border:1px solid #6ee7b7;border-radius:20px;padding:4px 14px;font-weight:bold;font-size:13px}"
             + ".footer{text-align:center;font-size:11px;color:#9ca3af;padding:16px}</style></head>"
             + "<body><div class='wrap'>"
             + "<div class='hdr'><h1 style='margin:0;font-size:22px'>Goods Received ✓</h1>"
             + "<p style='margin:4px 0 0;opacity:.85;font-size:13px'>" + name + "</p></div>"
             + "<div class='body'>"
             + "<p>Dear <strong>" + order.getSupplier().getName() + "</strong>,</p>"
             + "<p>We confirm that goods for <strong>" + order.getPoNumber() + "</strong> have been received.</p>"
             + "<table><tr><td><strong>PO Number</strong><br>" + order.getPoNumber() + "</td>"
             + "<td><strong>Received Date</strong><br>" + date + "</td>"
             + "<td><strong>Total Amount</strong><br>" + sym + fmt(order.getTotalAmount()) + "</td></tr></table>"
             + "<p><span class='badge'>✓ All items received</span></p>"
             + "<p style='margin-top:20px;color:#6b7280;font-size:13px'>Thank you for your prompt delivery.</p>"
             + "</div><div class='footer'>Automated email from " + name + ".</div></div></body></html>";
    }

    // ── Util ──────────────────────────────────────────────────────────────

    private String resolveSupplierEmail(PurchaseOrder order) {
        if (order.getSupplier() == null) {
            log.warn("⚠️  PO {} has no supplier", order.getPoNumber()); return null;
        }
        String email = order.getSupplier().getEmail();
        log.info("🔍 Supplier: name={} email={}", order.getSupplier().getName(), email);
        if (email == null || email.isBlank()) {
            log.warn("⚠️  Supplier '{}' has no email — skipped", order.getSupplier().getName()); return null;
        }
        return email;
    }

    private Long resolveStoreId(PurchaseOrder order) {
        try {
            if (order.getBranch() != null && order.getBranch().getStore() != null)
                return order.getBranch().getStore().getId();
        } catch (Exception e) {
            log.warn("Could not resolve storeId: {}", e.getMessage());
        }
        return null;
    }

    private String storeName(PurchaseOrder order) {
        try {
            if (order.getBranch() != null && order.getBranch().getStore() != null
                    && order.getBranch().getStore().getBrand() != null)
                return order.getBranch().getStore().getBrand();
        } catch (Exception ignored) {}
        return fromName;
    }

    private String sym(PurchaseOrder order) {
        try { return order.getCurrency() != null ? order.getCurrency().getSymbol() : "$"; }
        catch (Exception e) { return "$"; }
    }

    private String fmt(BigDecimal v) { return v != null ? String.format("%.2f", v) : "0.00"; }
    private boolean gt0(BigDecimal v) { return v != null && v.compareTo(BigDecimal.ZERO) > 0; }
}