package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentGoodsReceiptDTO {
    private Long      id;
    private String    grnNumber;
    private String    poNumber;
    private String    supplierName;
    private String    status;
    private LocalDate receivedDate;
    private Long      totalReceivedQuantity;
}
