package com.sh.sh.pos.system.payload.dto.stockDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferItemDTO {
     private Long id;

    private Long productId;
    private String productName;

    private Long variantId;
    private String variantName;

    private Long batchId;
    private String batchNo;

    private Integer requestedQty;
    private Integer shippedQty;
    private Integer receivedQty;
    private Integer damagedQty;
    private Integer missingQty;
}
