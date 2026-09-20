package com.sh.sh.pos.system.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


// ADAPT: your real notification service interface
import com.sh.sh.pos.system.service.inventoryservice.InventoryNotificationService;
import com.sh.sh.pos.system.service.inventoryservice.event.GoodsReceiptCompletedEvent;
import com.sh.sh.pos.system.service.inventoryservice.event.InventoryLowStockEvent;
import com.sh.sh.pos.system.service.inventoryservice.event.InventoryUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All @TransactionalEventListener methods use phase = AFTER_COMMIT.
 * This is the detail that matters most: if a listener fired on the default
 * phase and the outer transaction later rolled back, a "goods received"
 * notification could go out for a receipt that was never actually saved.
 * AFTER_COMMIT guarantees the event only fires once the write is durable.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryNotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGoodsReceiptCompleted(GoodsReceiptCompletedEvent event) {
        log.info("Goods receipt {} completed for PO {}", event.goodsReceiptId(), event.purchaseOrderId());
        notificationService.notifyGoodsReceiptCompleted(event.goodsReceiptId(), event.branchId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInventoryUpdated(InventoryUpdatedEvent event) {
        // ADAPT: invalidate InventoryDashboardService's cache here if you add
        // Redis caching to it, e.g. cacheManager.evict("inventory-dashboard", event.warehouseId())
        log.debug("Inventory {} updated to quantity {}", event.inventoryId(), event.newQuantity());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock(InventoryLowStockEvent event) {
        log.warn("Low stock: inventory {} available={} reorderLevel={}",
                event.inventoryId(), event.availableQuantity(), event.reorderLevel());
        notificationService.notifyLowStock(event.productId(), event.warehouseId(), event.availableQuantity());
    }
}
