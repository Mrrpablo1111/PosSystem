package com.sh.sh.pos.system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.model.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
  List<StockMovement> findByBranch_Id(Long branchId);

    List<StockMovement> findByProduct_Id(Long productId);

    List<StockMovement> findByVariant_Id(Long variantId);

    List<StockMovement> findByBatch_Id(Long batchId);

    List<StockMovement> findByMovementType(StockMovementType movementType);

    List<StockMovement> findByReferenceId(Long referenceId);

    List<StockMovement> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
