package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;

public interface EmailService {
    void sendEmail(String  to, String subject, String body);
       // ── Auth ──────────────────────────────────────────────────────────────
 
    /**
     * Welcome email when a new store admin or staff account is created.
     * @param to       new user's email
     * @param name     user's full name
     * @param tempPass temporary password — ask them to change on first login
     */
    void sendWelcomeEmail(String to, String name, String tempPass);
 
    // ── Purchase Orders ───────────────────────────────────────────────────
 
    /**
     * Sends PO HTML + PDF attachment to supplier.
     * Called when status moves DRAFT → PENDING.
     */
    void sendPurchaseOrderEmail(PurchaseOrder order);
 
    /**
     * Sends goods-received confirmation to supplier.
     * Called automatically when all items are fully received → RECEIVED.
     */
    void sendReceiptConfirmationEmail(PurchaseOrder order);
 
    /**
     * Overdue alert sent to the store manager.
     * Called by @Scheduled job when expectedDelivery < today
     * and status is still APPROVED or PARTIALLY_RECEIVED.
     *
     * @param order        the overdue PO
     * @param managerEmail store manager email from Store.contact.email
     */
    void sendOverduePOAlert(PurchaseOrder order, String managerEmail);
 
    // ── Reports & Alerts ─────────────────────────────────────────────────
 
    /**
     * Daily sales report sent each morning at 07:00.
     * HTML is built by ReportService and passed in.
     *
     * @param store       the store — recipient is store.contact.email
     * @param reportHtml  pre-built HTML report body
     */
    void sendDailySalesReport(Store store, String reportHtml);
 
    /**
     * Low stock alert when remainingQuantity drops below threshold.
     * Called by @Scheduled job every day at 07:00.
     *
     * @param store        the store
     * @param lowBatches   batches with remainingQuantity below minimum
     * @param managerEmail who to notify
     */
    void sendLowStockAlert(Store store, List<ProductBatch> lowBatches, String managerEmail);
 
    /**
     * Expiry alert when batch.expiryDate is within 30 days.
     * Called by @Scheduled job every day at 07:30.
     *
     * @param store            the store
     * @param expiringBatches  batches expiring soon with remaining stock
     * @param managerEmail     who to notify
     */
    void sendExpiryAlert(Store store, List<ProductBatch> expiringBatches, String managerEmail);
 
    /**
     * Monthly summary sent on the 1st of each month at 08:00.
     * HTML is built by ReportService and passed in.
     *
     * @param store       the store — recipient is store.contact.email
     * @param reportHtml  pre-built HTML report body
     */
    void sendMonthlyReport(Store store, String reportHtml);
    
}
