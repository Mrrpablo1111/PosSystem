package com.sh.sh.pos.system.payload.dto.priceDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceRuleDTO {
     private Long id;

    private Long productId;
    private String productName;

    private Long variantId;
    private String variantName;

    private Long priceGroupId;
    private String priceGroupName;

    private Long branchId;
    private String branchName;

    private BigDecimal fixedPrice;

    private BigDecimal discountPercent;

    private BigDecimal markupPercent;

    private Integer minQuantity;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer priority;

    private Boolean active;

    private LocalDateTime createdAt;
}
