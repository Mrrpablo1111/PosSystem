package com.sh.sh.pos.system.exception.inventoryexception;

/** No Inventory row exists for the given product/variant/warehouse. Maps to 404. */
public class InventoryNotFoundException extends InventoryException {
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
