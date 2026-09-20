package com.sh.sh.pos.system.service.inventoryservice;

/**
 * Only ever called from InventoryEventListener (event-driven), never
 * directly from InventoryService/GoodsReceiptService — this keeps
 * notification delivery swappable (email today, push tomorrow) without
 * touching a single line of inventory business logic.
 */
public interface InventoryNotificationService {

    void notifyGoodsReceiptCompleted(Long goodsReceiptId, Long branchId);

    void notifyLowStock(Long productId, Long warehouseId, int availableQuantity);

    void notifyOutOfStock(Long productId, Long warehouseId);
}
