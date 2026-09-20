package com.sh.sh.pos.system.service.inventoryservice.event;

/** Published after a GRN is saved as COMPLETED — listeners must use
 *  @TransactionalEventListener(phase = AFTER_COMMIT) so a later rollback
 *  can never cause a notification for data that was never actually saved. */
public record GoodsReceiptCompletedEvent(Long goodsReceiptId, Long purchaseOrderId, Long branchId) {
}
