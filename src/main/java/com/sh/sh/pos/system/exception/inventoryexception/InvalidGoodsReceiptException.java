package com.sh.sh.pos.system.exception.inventoryexception;

/** GRN already completed/cancelled, has no items, or is otherwise not actionable. Maps to 400. */
public class InvalidGoodsReceiptException extends InventoryException {
    public InvalidGoodsReceiptException(String message) {
        super(message);
    }
}
