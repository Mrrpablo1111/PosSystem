package com.sh.sh.pos.system.payload.dto.purchasesOrderItemDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDTO {

    private Long id;

    // ── Product ───────────────────────────────────────────────────────────

    private Long   productId;
    private String productName;   // resolved — frontend needs this for display

    // ── Variant (optional) ────────────────────────────────────────────────

    private Long   variantId;     // null if product has no variant selected
    private String variantName;   // null if no variant

    // ── Quantity tracking ─────────────────────────────────────────────────

    /** How many were ordered from the supplier */
    private Integer requestedQuantity;

    /** How many have arrived so far (0 on creation, updated on receive) */
    private Integer receivedQuantity;

    /** requestedQuantity - receivedQuantity */
    private Integer remainingQuantity;

    // ── Pricing ───────────────────────────────────────────────────────────

    private BigDecimal unitCost;

    /** Line-level discount percentage (0–100), 0 if no discount */
    private BigDecimal discountPct;

    /** unitCost × requestedQuantity × (1 - discountPct / 100) */
    private BigDecimal totalCost;

    // ── Notes ─────────────────────────────────────────────────────────────

    /** Optional — instructions at creation or damage note at receive time */
    private String note;

    // ── Computed flag ─────────────────────────────────────────────────────

    /**
     * True when remainingQuantity == 0.
     * Frontend uses this to disable the Receive button on fully received lines.
     */
    private boolean fullyReceived;
}