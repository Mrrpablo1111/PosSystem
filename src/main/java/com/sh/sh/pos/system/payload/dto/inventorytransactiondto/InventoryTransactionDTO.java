package com.sh.sh.pos.system.payload.dto.inventorytransactiondto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionDTO {

    private Long id;

    // ── Product ───────────────────────────────────────────────────────────
    private Long   productId;
    private String productName;
    private Long   variantId;
    private String variantName;

    // ── Location ──────────────────────────────────────────────────────────
    private Long   warehouseId;
    private String warehouseName;

    // ── Movement ──────────────────────────────────────────────────────────
    /** Positive = stock in, negative = stock out */
    private Integer quantity;
    private BigDecimal unitCost;

    /** IN / OUT — derived from the quantity sign for easy display */
    private String direction;

    // ── Source document ───────────────────────────────────────────────────
    private String referenceType;
    private Long   referenceId;

    private String performedByName;
    private String note;

    private LocalDateTime createdAt;
}
