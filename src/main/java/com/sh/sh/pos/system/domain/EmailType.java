package com.sh.sh.pos.system.domain;

public enum EmailType {
    // ── Purchase Orders ───────────────────────────────────────────────────
    PURCHASE_ORDER,         // ✓ already built — PO sent to supplier
    GOODS_RECEIVED,         // ✓ already built — receipt confirmation
    OVERDUE_PO_ALERT,       // expectedDelivery passed, goods not received
 
    // ── Reports & Alerts ─────────────────────────────────────────────────
    DAILY_SALES_REPORT,     // sent each morning with previous day summary
    LOW_STOCK_ALERT,        // product batch below minimum threshold
    EXPIRY_ALERT,           // batch expiring within 30 days
    MONTHLY_REPORT          // sent 1st of each month
}
 