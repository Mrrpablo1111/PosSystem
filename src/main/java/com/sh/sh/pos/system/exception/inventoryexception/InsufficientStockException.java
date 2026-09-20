package com.sh.sh.pos.system.exception.inventoryexception;

/** A decrease/reservation would push quantity or available stock below zero. Maps to 400/409. */
public class InsufficientStockException extends InventoryException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
