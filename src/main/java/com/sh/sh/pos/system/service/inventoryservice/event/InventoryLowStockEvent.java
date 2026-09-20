package com.sh.sh.pos.system.service.inventoryservice.event;

/** Published by InventoryService only when a mutation crosses the reorder
 *  threshold — i.e. available quantity went from above to at-or-below
 *  reorderLevel. Not fired on every read, only on the crossing itself. */
public record InventoryLowStockEvent(Long inventoryId, Long productId, Long warehouseId,
                                      int availableQuantity, int reorderLevel) {
}
