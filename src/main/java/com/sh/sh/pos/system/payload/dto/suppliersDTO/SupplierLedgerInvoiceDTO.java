package com.sh.sh.pos.system.payload.dto.suppliersDTO;


import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierLedgerInvoiceDTO {

    private Long invoiceId;
    private String invoiceNumber;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    private String paymentStatus;
}