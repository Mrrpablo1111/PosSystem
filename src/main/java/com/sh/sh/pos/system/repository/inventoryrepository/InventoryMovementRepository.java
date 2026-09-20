package com.sh.sh.pos.system.repository.inventoryrepository;



import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.domain.MovementType;
import com.sh.sh.pos.system.model.inventorymovement.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    // ── Basic reads ──────────────────────────────────────────────────────

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryMovement> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    List<InventoryMovement> findByProductIdAndBranchIdOrderByCreatedAtDesc(Long productId, Long branchId);

    List<InventoryMovement> findByType(MovementType type);

    /** Most recent N movements for an activity feed / product detail timeline. */
    List<InventoryMovement> findTop10ByProductIdOrderByCreatedAtDesc(Long productId);

    // ── Dashboard: "Today's Inventory Movement" ─────────────────────────

    @Query("""
        SELECT COUNT(m) FROM InventoryMovement m
        WHERE m.branch.id = :branchId
          AND m.createdAt >= :startOfDay
          AND m.createdAt < :endOfDay
        """)
    long countMovementsForBranchBetween(@Param("branchId") Long branchId,
                                         @Param("startOfDay") LocalDateTime startOfDay,
                                         @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
        SELECT m FROM InventoryMovement m
        WHERE m.branch.id = :branchId
          AND m.createdAt >= :startOfDay
          AND m.createdAt < :endOfDay
        ORDER BY m.createdAt DESC
        """)
    List<InventoryMovement> findMovementsForBranchBetween(@Param("branchId") Long branchId,
                                                           @Param("startOfDay") LocalDateTime startOfDay,
                                                           @Param("endOfDay") LocalDateTime endOfDay);

    // ── Dashboard: "Most Moved Products" ────────────────────────────────
    // Ranked by total absolute quantity moved — a product with 50 units
    // received AND 40 sold ranks above one with only 5 received, even
    // though the net change is smaller; that's deliberate: it measures
    // *activity*, not net stock change.

    @Query("""
        SELECT m.product.id, SUM(ABS(m.quantity)) AS totalMoved
        FROM InventoryMovement m
        WHERE m.branch.id = :branchId
          AND m.createdAt >= :since
        GROUP BY m.product.id
        ORDER BY totalMoved DESC
        """)
    List<Object[]> findMostMovedProductIds(@Param("branchId") Long branchId,
                                            @Param("since") LocalDateTime since,
                                            Pageable pageable);

    // ── Reports: Stock Movement / Movement History ─────────────────────

    @Query("""
        SELECT m FROM InventoryMovement m
        WHERE m.branch.id = :branchId
          AND (:type IS NULL OR m.type = :type)
          AND (:productId IS NULL OR m.product.id = :productId)
          AND m.createdAt BETWEEN :from AND :to
        ORDER BY m.createdAt DESC
        """)
    List<InventoryMovement> findMovementHistory(@Param("branchId") Long branchId,
                                                 @Param("type") MovementType type,
                                                 @Param("productId") Long productId,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to,
                                                 Pageable pageable);
}