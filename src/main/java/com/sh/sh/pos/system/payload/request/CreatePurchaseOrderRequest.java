package com.sh.sh.pos.system.payload.request;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class CreatePurchaseOrderRequest {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Branch is required")
    private Long branchId;

    /**
     * Currency ID referencing the currencies table.
     * Frontend sends the selected Currency's id.
     */
    private Long currencyId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate expectedDelivery;
    private String referenceNo;
    private Integer paymentTermDays;

    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;

    private String carrierId;
    private String trackingNo;

    @Size(max = 500)
    private String notes;

    @NotNull
    @Valid
    @Size(min = 1, message = "At least one item is required")
    private List<PurchaseOrderItemRequest> items;

    // ── nested item ───────────────────────────────────────────────────────

    @Data 
    @Builder 
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class PurchaseOrderItemRequest {

        @NotNull(message = "Product is required")
        private Long productId;

        private Long variantId;

        @NotNull(message = "Quantity is required")
        private Integer requestedQuantity;

        @NotNull(message = "Unit cost is required")
        private BigDecimal unitCost;

        private BigDecimal discountPct;

        private String note;
    }
}