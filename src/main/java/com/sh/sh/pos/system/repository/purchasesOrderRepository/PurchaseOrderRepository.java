package com.sh.sh.pos.system.repository.purchasesOrderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    // ── Used by service ───────────────────────────────────────────────────

    List<PurchaseOrder> findByBranchStoreId(Long storeId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.branch.store.id = :storeId")
    List<PurchaseOrder> findByStoreIdExplicit(@Param("storeId") Long storeId);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.branch b LEFT JOIN FETCH b.store s")
    List<PurchaseOrder> findAllWithBranchAndStore();

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    // ── Filter by supplier ────────────────────────────────────────────────

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    List<PurchaseOrder> findBySupplierIdAndBranchStoreId(Long supplierId, Long storeId);

    // ── Filter by status ──────────────────────────────────────────────────

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    List<PurchaseOrder> findByBranchStoreIdAndStatus(Long storeId, PurchaseOrderStatus status);

    // ── Filter by branch ──────────────────────────────────────────────────

    List<PurchaseOrder> findByBranchId(Long branchId);

    // ── Overdue detection ─────────────────────────────────────────────────

    @Query("""
            SELECT po FROM PurchaseOrder po
            WHERE po.expectedDelivery < :today
              AND po.receivedDate IS NULL
              AND po.status IN ('APPROVED', 'PARTIALLY_RECEIVED')
            """)
    List<PurchaseOrder> findOverdue(@Param("today") LocalDate today);

    // ── Used by PurchaseInvoiceService ────────────────────────────────────

    boolean existsBySupplierIdAndStatus(Long supplierId, PurchaseOrderStatus status);

    // ── NEW: used by EmailWorker for PDF generation ───────────────────────

    /**
     * Loads PO with ALL relations eagerly using JOIN FETCH.
     *
     * This is required by EmailWorker because @Scheduled runs outside
     * any @Transactional scope — the Hibernate session closes after the
     * initial findById(), so lazy collections like items, supplier, branch,
     * and store throw LazyInitializationException when accessed.
     *
     * JOIN FETCH loads everything in a single query while the session is open.
     */
    @Query("""
            SELECT po FROM PurchaseOrder po
            LEFT JOIN FETCH po.supplier
            LEFT JOIN FETCH po.branch b
            LEFT JOIN FETCH b.store
            LEFT JOIN FETCH po.currency
            LEFT JOIN FETCH po.items i
            LEFT JOIN FETCH i.product
            LEFT JOIN FETCH i.variant
            WHERE po.id = :id
            """)
    Optional<PurchaseOrder> findByIdWithAllRelations(@Param("id") Long id);
}