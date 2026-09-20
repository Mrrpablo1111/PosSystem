package com.sh.sh.pos.system.repository.inventorytransactionrepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.inventorytransaction.InventoryTransaction;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    // Spring Data resolves these via nested-property traversal
    // (product.id, warehouse.id) — no @Query needed for the simple cases.

    List<InventoryTransaction> findByProduct_IdOrderByCreatedAtDesc(Long productId);

    List<InventoryTransaction> findByWarehouse_IdOrderByCreatedAtDesc(Long warehouseId);

    List<InventoryTransaction> findByProduct_IdAndWarehouse_IdOrderByCreatedAtDesc(
            Long productId, Long warehouseId);

    List<InventoryTransaction> findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
            String referenceType, Long referenceId);

    List<InventoryTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to);

    /** Backs InventoryTransactionService.getAll(page, size) — plain
     *  paginated listing, most recent first. */
    Page<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Ledger-derived net quantity for one item — must equal the Inventory
     * table's current quantity if every mutation went through this ledger.
     * The (:variantId IS NULL AND ... IS NULL) OR ... pattern is required
     * for the same reason it is everywhere else in this codebase: a plain
     * `= :variantId` with a null parameter matches nothing in MySQL.
     */
    @Query("""
        SELECT COALESCE(SUM(t.quantity), 0) FROM InventoryTransaction t
        WHERE t.product.id = :productId
          AND t.warehouse.id = :warehouseId
          AND ((:variantId IS NULL AND t.productVariant IS NULL)
            OR t.productVariant.id = :variantId)
        """)
    Long sumNetQuantity(@Param("productId") Long productId,
                         @Param("variantId") Long variantId,
                         @Param("warehouseId") Long warehouseId);
}
