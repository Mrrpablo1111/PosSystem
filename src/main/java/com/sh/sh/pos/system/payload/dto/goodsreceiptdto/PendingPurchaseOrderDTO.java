package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POs approved/sent that still have goods outstanding. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingPurchaseOrderDTO {
    private Long      purchaseOrderId;
    private String    poNumber;
    private String    supplierName;
    private String    status;
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private Long      remainingQuantity;
}
