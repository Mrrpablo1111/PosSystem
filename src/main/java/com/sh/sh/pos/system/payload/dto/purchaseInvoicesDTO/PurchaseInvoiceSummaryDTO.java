package com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceSummaryDTO {

    private Long id;
    private String invoiceNumber;
    private String status;
}