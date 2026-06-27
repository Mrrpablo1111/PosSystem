package com.sh.sh.pos.system.payload.dto.productDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.ExpiryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchDTO {
    private Long id;

    private Long branchId;

    private Long productId;
    private String productName;
    private String batchNo;
    private BigDecimal purchasePrice;
    private Long variantId;
    private String variantName;
    private String supplierName;
    private ExpiryStatus expiryStatus;
    private long daysLeft;
    private Integer quantity;

    private Integer remainingQuantity; 
    
    private Long supplierId;

    private LocalDate expiryDate;
    private LocalDateTime createdAt;
}
