package com.sh.sh.pos.system.enums;

/**
 * Lifecycle of a Goods Receipt (GRN).
 *
 * DRAFT      → created by warehouse staff, editable, no stock impact
 * RECEIVING  → goods physically arriving / being checked
 * COMPLETED  → approved; PO quantities updated, batches + inventory posted
 * CANCELLED  → voided; never affected stock
 */
public enum GoodsReceiptStatus {
    DRAFT,
    RECEIVING,
    COMPLETED,
    CANCELLED
}
