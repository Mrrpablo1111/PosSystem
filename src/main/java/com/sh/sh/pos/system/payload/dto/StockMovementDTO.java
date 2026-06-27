package com.sh.sh.pos.system.payload.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.ReferenceType;
import com.sh.sh.pos.system.domain.StockMovementType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDTO {
    private Long id;

    private Long branchId;
    private String branchName;

    private Long productId;
    private String productName;

    private Long variantId;
    private String variantName;

    private Long batchId;
    private String batchNo;

    private StockMovementType movementType;

    private Integer quantity;

    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private ReferenceType referenceType;

    private Long referenceId;

    private String note;

    private LocalDateTime createdAt;
}
