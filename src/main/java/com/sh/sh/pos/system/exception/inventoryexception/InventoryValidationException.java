package com.sh.sh.pos.system.exception.inventoryexception;

/** Catch-all for validation failures not covered by a more specific exception. Maps to 400. */
public class InventoryValidationException extends InventoryException {
    public InventoryValidationException(String message) {
        super(message);
    }
}
