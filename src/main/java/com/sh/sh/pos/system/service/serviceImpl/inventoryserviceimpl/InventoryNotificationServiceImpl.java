package com.sh.sh.pos.system.service.serviceimpl.inventoryserviceimpl;



import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.service.inventoryservice.InventoryNotificationService;

import lombok.extern.slf4j.Slf4j;

/**
 * STUB — logs instead of sending a real notification. This exists so the
 * app can start and InventoryEventListener has a bean to inject; it is
 * NOT wired to email/in-app delivery yet.
 *
 * To make this real: follow whatever pattern your app already uses
 * elsewhere (e.g. the same mechanism behind your password-reset email in
 * EmailUtil.java, or an in-app Notification entity/table if you have one).
 * Paste that existing sender and I'll wire these three methods to it
 * directly instead of leaving them as log lines.
 */
@Service
@Slf4j
public class InventoryNotificationServiceImpl implements InventoryNotificationService {

    @Override
    public void notifyGoodsReceiptCompleted(Long goodsReceiptId, Long branchId) {
        log.info("[STUB NOTIFICATION] Goods receipt {} completed for branch {}", goodsReceiptId, branchId);
        // ADAPT: send an in-app notification / email to branch staff here.
    }

    @Override
    public void notifyLowStock(Long productId, Long warehouseId, int availableQuantity) {
        log.warn("[STUB NOTIFICATION] Low stock — product {} in warehouse {}: {} available",
                productId, warehouseId, availableQuantity);
        // ADAPT: notify the store manager / purchasing staff.
    }

    @Override
    public void notifyOutOfStock(Long productId, Long warehouseId) {
        log.warn("[STUB NOTIFICATION] OUT OF STOCK — product {} in warehouse {}", productId, warehouseId);
        // ADAPT: same delivery mechanism as notifyLowStock, higher urgency.
    }
}