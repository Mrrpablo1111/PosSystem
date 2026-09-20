package com.sh.sh.pos.system.repository.goodreceiptrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.goodreceipt.GoodsReceiptItem;


public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Long> {

    List<GoodsReceiptItem> findByGoodsReceiptId(Long goodsReceiptId);

    /** All receipt lines ever recorded against one PO line (audit trail). */
    @Query("""
        SELECT i FROM GoodsReceiptItem i
        WHERE i.purchaseOrderItem.id = :poItemId
          AND i.goodsReceipt.status <> 'CANCELLED'
        ORDER BY i.goodsReceipt.receivedDate ASC
        """)
    List<GoodsReceiptItem> findHistoryByPurchaseOrderItem(@Param("poItemId") Long poItemId);
}
