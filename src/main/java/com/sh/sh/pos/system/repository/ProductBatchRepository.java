package com.sh.sh.pos.system.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.products.ProductBatch;

public interface ProductBatchRepository
                extends JpaRepository<ProductBatch, Long> {

        List<ProductBatch> findByProductId(Long productId);

        List<ProductBatch> findByBranchId(Long branchId);

        List<ProductBatch> findByBatchNoContainingIgnoreCase(String batchNo);

        List<ProductBatch> findByExpiryDateBetween(LocalDate start, LocalDate end);

        List<ProductBatch> findByRemainingQuantityGreaterThan(Integer qty);

        List<ProductBatch> findByProductIdAndBranchIdAndRemainingQuantityGreaterThanOrderByExpiryDateAsc(
                        Long productId, Long branchId, Integer remainingQuantity);

        List<ProductBatch> findBySupplierId(Long supplierId);

        Optional<ProductBatch> findByProductIdAndBranchIdAndBatchNo(
                        Long productId,
                        Long branchId,
                        String batchNo);

        // ── NEW — required by EmailScheduler ─────────────────────────────────

        /**
         * Used by EmailScheduler.checkLowStock()
         * Finds all batches in a store where remainingQuantity <= threshold (e.g. 10).
         * Navigates: ProductBatch → branch → store → id
         */
        List<ProductBatch> findByBranchStoreIdAndRemainingQuantityLessThanEqual(
                        Long storeId, int remainingQuantity);

        /**
         * Used by EmailScheduler.checkExpiringBatches()
         * Finds all batches in a store expiring before a given date (e.g. today + 30
         * days).
         * Navigates: ProductBatch → branch → store → id
         */
        List<ProductBatch> findByBranchStoreIdAndExpiryDateBefore(
                        Long storeId, LocalDate expiryDate);

        // ── BONUS — useful for inventory management ───────────────────────────

        /**
         * All batches for a store — used for inventory overview page.
         * Navigates: ProductBatch → branch → store → id
         */
        List<ProductBatch> findByBranchStoreId(Long storeId);

        /**
         * All expired batches in a store (expiryDate before today).
         * Useful for an "expired stock" report.
         */
        List<ProductBatch> findByBranchStoreIdAndExpiryDateBeforeAndRemainingQuantityGreaterThan(
                        Long storeId, LocalDate expiryDate, int remainingQuantity);

        /**
         * All batches for a specific product across all branches of a store.
         * Used to check total stock of a product store-wide.
         */
        List<ProductBatch> findByProductIdAndBranchStoreId(Long productId, Long storeId);
}
