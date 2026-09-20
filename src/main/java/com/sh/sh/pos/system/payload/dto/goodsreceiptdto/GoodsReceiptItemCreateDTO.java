package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemCreateDTO {

    @NotNull(message = "purchaseOrderItemId is required")
    private Long purchaseOrderItemId;

    /** Physically delivered in this receipt. Must be > 0 and <= remaining. */
    @NotNull(message = "receivedQuantity is required")
    @Min(value = 1, message = "receivedQuantity must be at least 1")
    private Integer receivedQuantity;

    /** Passed inspection. accepted + damaged + rejected must equal received. */
    @NotNull(message = "acceptedQuantity is required")
    @Min(value = 0, message = "acceptedQuantity cannot be negative")
    private Integer acceptedQuantity;

    @Min(value = 0, message = "damagedQuantity cannot be negative")
    private Integer damagedQuantity;

    @Min(value = 0, message = "rejectedQuantity cannot be negative")
    private Integer rejectedQuantity;

    @Size(max = 60, message = "batchNumber must be at most 60 characters")
    private String batchNumber;

    private LocalDate expiryDate;
    private LocalDate manufactureDate;

    @Size(max = 500, message = "note must be at most 500 characters")
    private String note;
}
