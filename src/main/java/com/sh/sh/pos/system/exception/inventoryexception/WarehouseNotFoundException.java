package com.sh.sh.pos.system.exception.inventoryexception;

/** Warehouse missing or inactive for the requested operation. Maps to 404. */
public class WarehouseNotFoundException extends InventoryException {
    public WarehouseNotFoundException(String message) {
        super(message);
    }
}
