package com.sh.sh.pos.system.repository.stockmovementrepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.model.stockmovement.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
  List<StockMovement> findByBranchId(Long branchId);

    List<StockMovement> findByProductId(Long productId);

    List<StockMovement> findByVariantId(Long variantId);

    List<StockMovement> findByBatchId(Long batchId);

    List<StockMovement> findByMovementType(StockMovementType movementType);

    List<StockMovement> findByReferenceId(Long referenceId);

    List<StockMovement> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
