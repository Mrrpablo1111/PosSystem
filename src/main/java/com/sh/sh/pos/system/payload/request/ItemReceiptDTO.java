package com.sh.sh.pos.system.payload.request;

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
public class ItemReceiptDTO {
    @NotNull(message = "Purchase order item ID is required")
    private Long purchaseOrderItemId;

    /**
     * Quantity received in THIS delivery batch.
     * Gets added to the existing receivedQuantity on the item.
     * Must be at least 1.
     */
    @NotNull(message = "Quantity received is required")
    @Min(value = 1, message = "Quantity received must be at least 1")
    private Integer quantityReceived;

    /**
     * Optional per-item note.
     * e.g. "2 units arrived damaged", "check expiry date"
     */
    @Size(max = 300, message = "Note must be at most 300 characters")
    private String note;
}
