package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSupplierDTO {
    private Long   supplierId;
    private String supplierName;
    private Long   receiptCount;
    private Long   totalUnitsReceived;
}
