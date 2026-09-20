package com.sh.sh.pos.system.service.inventoryservice.event;

/** Published by InventoryService after any quantity mutation (increase,
 *  decrease, reserve, release) — lets the dashboard invalidate its cache
 *  without InventoryService knowing the dashboard exists. */
public record InventoryUpdatedEvent(Long inventoryId, Long productId, Long warehouseId, int newQuantity) {
}
