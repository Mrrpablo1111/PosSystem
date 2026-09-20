package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Lightweight row for list endpoints — no item collection, cheap to map. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptResponseDTO {

    private Long   id;
    private String grnNumber;
    private String status;

    private Long   purchaseOrderId;
    private String poNumber;

    private String supplierName;
    private String warehouseName;

    private LocalDate receivedDate;
    private String    receivedByName;

    private Integer itemCount;
    private Integer totalReceivedQuantity;
    private Integer totalDamagedQuantity;

    private LocalDateTime createdAt;
}
