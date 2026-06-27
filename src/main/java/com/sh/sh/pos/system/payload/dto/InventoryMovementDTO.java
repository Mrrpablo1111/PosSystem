package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.MovementType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryMovementDTO {
    private Long id;
    private Long branchId;
    private Long productId;
    private MovementType type;
    private Integer quantity;
    private String note;
    private LocalDateTime createdAt;
}
