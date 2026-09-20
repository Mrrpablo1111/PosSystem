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

/** Only DRAFT receipts can be updated. Items are replaced wholesale. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptUpdateDTO {

    @NotNull(message = "warehouseId is required")
    private Long warehouseId;

    private LocalDate receivedDate;

    @Size(max = 500, message = "note must be at most 500 characters")
    private String note;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GoodsReceiptItemCreateDTO> items;
}
