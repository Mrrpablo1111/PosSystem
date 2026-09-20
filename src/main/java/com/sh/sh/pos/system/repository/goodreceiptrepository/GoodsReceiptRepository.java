package com.sh.sh.pos.system.repository.goodreceiptrepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.enums.GoodsReceiptStatus;
import com.sh.sh.pos.system.model.goodreceipt.GoodsReceipt;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.MonthlyReceivingTrendDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.PendingPurchaseOrderDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.RecentGoodsReceiptDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.TopSupplierDTO;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    // ── Lookups ───────────────────────────────────────────────────────────

    Optional<GoodsReceipt> findByGrnNumber(String grnNumber);

    boolean existsByGrnNumber(String grnNumber);

    @EntityGraph(attributePaths = {"items", "supplier", "warehouse", "branch", "purchaseOrder"})
    Optional<GoodsReceipt> findWithItemsById(Long id);

    List<GoodsReceipt> findByPurchaseOrderIdOrderByCreatedAtDesc(Long purchaseOrderId);

    List<GoodsReceipt> findByStatusOrderByCreatedAtDesc(GoodsReceiptStatus status);

    long countByGrnNumberStartingWith(String prefix);

    // ── Dashboard counters ────────────────────────────────────────────────

    @Query("SELECT COUNT(g) FROM GoodsReceipt g WHERE g.receivedDate = :today AND g.status <> 'CANCELLED'")
    long countTodayReceipts(@Param("today") LocalDate today);

    @Query("SELECT COUNT(g) FROM GoodsReceipt g WHERE g.status IN :statuses")
    long countByStatusIn(@Param("statuses") Collection<GoodsReceiptStatus> statuses);

    @Query("""
        SELECT COALESCE(SUM(i.damagedQuantity), 0)
        FROM GoodsReceiptItem i
        WHERE i.goodsReceipt.status = 'COMPLETED'
        """)
    long sumDamagedItems();

    // ── Dashboard lists ───────────────────────────────────────────────────

    @Query("""
        SELECT new com.sh.sh.pos.system.payload.dto.goodsreceiptdto.RecentGoodsReceiptDTO(
            g.id, g.grnNumber, g.purchaseOrder.poNumber, g.supplier.name,
            CAST(g.status AS string), g.receivedDate,
            COALESCE(SUM(i.receivedQuantity), 0))
        FROM GoodsReceipt g LEFT JOIN g.items i
        GROUP BY g.id, g.grnNumber, g.purchaseOrder.poNumber, g.supplier.name, g.status, g.receivedDate, g.createdAt
        ORDER BY g.createdAt DESC
        """)
    List<RecentGoodsReceiptDTO> findRecentReceipts(Pageable pageable);

    @Query("""
        SELECT new com.sh.sh.pos.system.payload.dto.goodsreceiptdto.TopSupplierDTO(
            g.supplier.id, g.supplier.name, COUNT(DISTINCT g.id),
            COALESCE(SUM(i.receivedQuantity), 0))
        FROM GoodsReceipt g LEFT JOIN g.items i
        WHERE g.status = 'COMPLETED'
        GROUP BY g.supplier.id, g.supplier.name
        ORDER BY COUNT(DISTINCT g.id) DESC
        """)
    List<TopSupplierDTO> findTopSuppliers(Pageable pageable);

    /**
     * ADAPT: PO status literals must match your PurchaseOrderStatus enum names.
     */
    @Query("""
        SELECT new com.sh.sh.pos.system.payload.dto.goodsreceiptdto.PendingPurchaseOrderDTO(
            po.id, po.poNumber, po.supplier.name, CAST(po.status AS string),
            po.orderDate, po.expectedDelivery,
            COALESCE(SUM(poi.remainingQuantity), 0))
        FROM PurchaseOrder po JOIN po.items poi
        WHERE po.status IN ('APPROVED', 'SENT', 'PARTIALLY_RECEIVED')
        GROUP BY po.id, po.poNumber, po.supplier.name, po.status, po.orderDate, po.expectedDelivery
        HAVING SUM(poi.remainingQuantity) > 0
        ORDER BY po.orderDate ASC
        """)
    List<PendingPurchaseOrderDTO> findPendingPurchaseOrders(Pageable pageable);

    @Query("""
        SELECT new com.sh.sh.pos.system.payload.dto.goodsreceiptdto.MonthlyReceivingTrendDTO(
            YEAR(g.receivedDate), MONTH(g.receivedDate),
            COUNT(DISTINCT g.id), COALESCE(SUM(i.receivedQuantity), 0))
        FROM GoodsReceipt g LEFT JOIN g.items i
        WHERE g.status = 'COMPLETED' AND g.receivedDate >= :from
        GROUP BY YEAR(g.receivedDate), MONTH(g.receivedDate)
        ORDER BY YEAR(g.receivedDate), MONTH(g.receivedDate)
        """)
    List<MonthlyReceivingTrendDTO> findMonthlyTrend(@Param("from") LocalDate from);
}
