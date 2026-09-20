package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Supplier and branch are NOT accepted from the client —
 * they are derived from the Purchase Order server-side so they can never mismatch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptCreateDTO {

    @NotNull(message = "purchaseOrderId is required")
    private Long purchaseOrderId;

    @NotNull(message = "warehouseId is required")
    private Long warehouseId;

    /** Defaults to today when null */
    private LocalDate receivedDate;

    @Size(max = 500, message = "note must be at most 500 characters")
    private String note;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GoodsReceiptItemCreateDTO> items;
}
