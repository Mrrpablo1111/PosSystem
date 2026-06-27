package com.sh.sh.pos.system.mapper;

import java.math.BigDecimal;

import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.StockMovement;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.StockMovementDTO;

public class StockMovementMapper {
     public static StockMovementDTO toDTO(StockMovement movement) {
        return StockMovementDTO.builder()
                .id(movement.getId())

                .branchId(movement.getBranch() != null ? movement.getBranch().getId() : null)
                .branchName(movement.getBranch() != null ? movement.getBranch().getName() : null)

                .productId(movement.getProduct() != null ? movement.getProduct().getId() : null)
                .productName(movement.getProduct() != null ? movement.getProduct().getName() : null)

                .variantId(movement.getVariant() != null ? movement.getVariant().getId() : null)
                .variantName(movement.getVariant() != null ? movement.getVariant().getName() : null)

                .batchId(movement.getBatch() != null ? movement.getBatch().getId() : null)
                .batchNo(movement.getBatch() != null ? movement.getBatch().getBatchNo() : null)

                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .unitCost(movement.getUnitCost())
                .totalCost(movement.getTotalCost())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .note(movement.getNote())
                .createdAt(movement.getCreatedAt())
                .build();
    }

    public static StockMovement toEntity(
            StockMovementDTO dto,
            Branch branch,
            Product product,
            ProductVariant variant,
            ProductBatch batch) {

        BigDecimal totalCost = dto.getUnitCost() != null && dto.getQuantity() != null
                ? dto.getUnitCost().multiply(BigDecimal.valueOf(dto.getQuantity()))
                : BigDecimal.ZERO;

        return StockMovement.builder()
                .id(dto.getId())
                .branch(branch)
                .product(product)
                .variant(variant)
                .batch(batch)
                .movementType(dto.getMovementType())
                .quantity(dto.getQuantity())
                .unitCost(dto.getUnitCost())
                .totalCost(totalCost)
                .referenceId(dto.getReferenceId())
                .referenceType(dto.getReferenceType())
                .note(dto.getNote())
                .build();
    }
}
