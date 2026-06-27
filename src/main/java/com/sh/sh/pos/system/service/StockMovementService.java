package com.sh.sh.pos.system.service;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.payload.dto.StockMovementDTO;

public interface StockMovementService {

     StockMovementDTO create(StockMovementDTO dto);

    StockMovementDTO get(Long id);

    List<StockMovementDTO> getAll();

    List<StockMovementDTO> getByBranch(Long branchId);

    List<StockMovementDTO> getByProduct(Long productId);

    List<StockMovementDTO> getByVariant(Long variantId);

    List<StockMovementDTO> getByBatch(Long batchId);

    List<StockMovementDTO> getByType(StockMovementType movementType);

    List<StockMovementDTO> getByDateRange(LocalDateTime start, LocalDateTime end);
    
}
