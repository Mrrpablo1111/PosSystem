package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemDTO {

    private Long id;

    private Long purchaseOrderItemId;

    // ── Product (resolved for display) ────────────────────────────────────
    private Long   productId;
    private String productName;
    private Long   variantId;
    private String variantName;

    // ── Quantities ────────────────────────────────────────────────────────
    private Integer requestedQuantity;
    private Integer receivedQuantity;
    private Integer acceptedQuantity;
    private Integer damagedQuantity;
    private Integer rejectedQuantity;

    // ── Pricing snapshot ──────────────────────────────────────────────────
    private BigDecimal unitCost;

    // ── Batch tracking ────────────────────────────────────────────────────
    private String    batchNumber;
    private LocalDate expiryDate;
    private LocalDate manufactureDate;

    private String note;
}
