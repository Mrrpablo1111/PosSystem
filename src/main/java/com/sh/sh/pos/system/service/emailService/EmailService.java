package com.sh.sh.pos.system.service.emailservice;

import java.util.List;

import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.store.Store;

public interface EmailService {

    void sendEmail(
            String to,
            String subject,
            String body
    );

    // =========================================================
    // AUTH
    // =========================================================

    void sendWelcomeEmail(
            String to,
            String name,
            String tempPass
    );

    void sendPasswordResetOtpEmail(
            String to,
            String name,
            String otp
    );

    void sendVerificationOtpEmail(
            String to, 
            String name,
            String otp
    );

    // =========================================================
    // PURCHASE ORDERS
    // =========================================================

    void sendPurchaseOrderEmail(
            PurchaseOrder order
    );

    void sendReceiptConfirmationEmail(
            PurchaseOrder order
    );

    void sendOverduePOAlert(
            PurchaseOrder order,
            String managerEmail
    );

    void sendOutstandingItemsReminderEmail(
            PurchaseOrder order
    );

    // =========================================================
    // REPORTS & ALERTS
    // =========================================================

    void sendDailySalesReport(
            Store store,
            String reportHtml
    );

    void sendLowStockAlert(
            Store store,
            List<ProductBatch> lowBatches,
            String managerEmail
    );

    void sendExpiryAlert(
            Store store,
            List<ProductBatch> expiringBatches,
            String managerEmail
    );

    void sendMonthlyReport(
            Store store,
            String reportHtml
    );
}
