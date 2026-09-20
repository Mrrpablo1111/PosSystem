package com.sh.sh.pos.system.repository.purchaseorderitemrepository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;



public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    // ── Basic lookups ─────────────────────────────────────────────────────

    /** All lines on one purchase order */
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);

    /** One line, but only if it belongs to the given PO — safer than findById
     *  when the PO id comes from the request path (prevents cross-PO access) */
    Optional<PurchaseOrderItem> findByIdAndPurchaseOrderId(Long id, Long purchaseOrderId);

    // ── Receiving / GRN support ───────────────────────────────────────────

    /** Lines still waiting for goods on one PO */
    @Query("""
        SELECT i FROM PurchaseOrderItem i
        WHERE i.purchaseOrder.id = :purchaseOrderId
          AND i.remainingQuantity > 0
        """)
    List<PurchaseOrderItem> findOutstandingByPurchaseOrder(@Param("purchaseOrderId") Long purchaseOrderId);

    /** True when every line on the PO is fully received —
     *  cheaper than loading the whole item list just to check status */
    @Query("""
        SELECT COUNT(i) = 0 FROM PurchaseOrderItem i
        WHERE i.purchaseOrder.id = :purchaseOrderId
          AND i.remainingQuantity > 0
        """)
    boolean isFullyReceived(@Param("purchaseOrderId") Long purchaseOrderId);

    /** Total units still outstanding on one PO (0 when fully received) */
    @Query("""
        SELECT COALESCE(SUM(i.remainingQuantity), 0) FROM PurchaseOrderItem i
        WHERE i.purchaseOrder.id = :purchaseOrderId
        """)
    long sumRemainingByPurchaseOrder(@Param("purchaseOrderId") Long purchaseOrderId);

    // ── Reporting ─────────────────────────────────────────────────────────

    /** Every open PO line for a product across all orders —
     *  "how much of this product is already on order?" (incoming stock) */
    @Query("""
        SELECT i FROM PurchaseOrderItem i
        WHERE i.product.id = :productId
          AND i.remainingQuantity > 0
          AND i.purchaseOrder.status IN ('APPROVED', 'SENT', 'PARTIALLY_RECEIVED')
        """)
    List<PurchaseOrderItem> findOpenLinesByProduct(@Param("productId") Long productId);

    /** Total incoming quantity for a product (optionally variant-specific) */
    @Query("""
        SELECT COALESCE(SUM(i.remainingQuantity), 0) FROM PurchaseOrderItem i
        WHERE i.product.id = :productId
          AND (:variantId IS NULL OR i.variant.id = :variantId)
          AND i.purchaseOrder.status IN ('APPROVED', 'SENT', 'PARTIALLY_RECEIVED')
        """)
    long sumIncomingQuantity(@Param("productId") Long productId,
                             @Param("variantId") Long variantId);
}