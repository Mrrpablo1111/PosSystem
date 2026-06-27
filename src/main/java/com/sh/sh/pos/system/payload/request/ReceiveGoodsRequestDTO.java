package com.sh.sh.pos.system.payload.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
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
public class ReceiveGoodsRequestDTO {
     @NotNull(message = "Purchase order ID is required")
    private Long purchaseOrderId;
    private LocalDate receivedDate;
    @Size(max = 300, message = "Note must be at most 300 characters")
    private String note;
    @NotNull(message = "Items are required")
    @Size(min = 1, message = "At least one item is required")
    @Valid
    private List<ItemReceiptDTO> items;

    
}
