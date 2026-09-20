package com.sh.sh.pos.system.repository.inventoryrepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.inventory.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // ── Existing (kept for compatibility) ─────────────────────────────────

    /**
     * @deprecated returns a single row, but one product can now have stock
     * rows in several warehouses/variants — this throws
     * NonUniqueResultException as soon as the second row exists.
     * Use {@link #findByProductIdOrderByWarehouseIdAsc(Long)} instead.
     */
    @Deprecated
    Inventory findByProductId(Long productId);

    List<Inventory> findByBranchId(Long branchId);

    /**
     * Kept as-is for the dashboard. Note it uses a hardcoded threshold of 5;
     * per-row reorderLevel filtering is available via
     * InventoryService.getLowStockByBranch().
     */
    @Query("""
        SELECT COUNT(i)
        FROM Inventory i
        JOIN i.product p
        WHERE i.branch.id = :branchId
        AND i.quantity <= 5
    """)
    int countLowStockItems(@Param("branchId") Long branchId);

    // ── GRN + InventoryServiceImpl integration ────────────────────────────

    /**
     * The exact row a goods receipt or manual adjustment targets.
     *
     * Must be a custom @Query: a derived method with a null variantId would
     * generate `product_variant_id = NULL` in SQL, which matches NOTHING in
     * MySQL — every receipt of a variant-less product would then create a
     * duplicate row. The explicit IS NULL branch handles it correctly.
     */
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.product.id = :productId
          AND i.warehouse.id = :warehouseId
          AND ((:variantId IS NULL AND i.productVariant IS NULL)
            OR i.productVariant.id = :variantId)
        """)
    Optional<Inventory> findByProductIdAndProductVariantIdAndWarehouseId(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("warehouseId") Long warehouseId);

    // ── Reads used by InventoryServiceImpl ────────────────────────────────

    List<Inventory> findByWarehouseId(Long warehouseId);

    /** All stock rows for a product across warehouses/variants */
    List<Inventory> findByProductIdOrderByWarehouseIdAsc(Long productId);

    // ── Reporting helpers ─────────────────────────────────────────────────

    /** Total on-hand for a product across all warehouses of one branch */
    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i
        WHERE i.product.id = :productId
          AND i.branch.id = :branchId
        """)
    long sumQuantityByProductAndBranch(@Param("productId") Long productId,
                                       @Param("branchId") Long branchId);

    /** Low-stock rows using each row's own reorderLevel (available = qty - reserved) */
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.branch.id = :branchId
          AND (i.quantity - i.reservedQuantity) <= i.reorderLevel
        ORDER BY (i.quantity - i.reservedQuantity) ASC
        """)
    List<Inventory> findLowStockByBranch(@Param("branchId") Long branchId);


    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i
        WHERE i.product.id = :productId
          AND ((:variantId IS NULL AND i.productVariant IS NULL)
            OR i.productVariant.id = :variantId)
        """)
    Integer sumQuantityForItem(@Param("productId") Long productId,
                               @Param("variantId") Long variantId);
}