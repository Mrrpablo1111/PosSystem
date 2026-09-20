package com.sh.sh.pos.system.exception.inventoryexception;

/** Base type — lets a single @ExceptionHandler(InventoryException.class) catch the whole family. */
public abstract class InventoryException extends RuntimeException {
    protected InventoryException(String message) {
        super(message);
    }
}
