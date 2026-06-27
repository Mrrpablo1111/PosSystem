package com.sh.sh.pos.system.payload.dto.suppliersDTO;


import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierLedgerDTO {

    private Long supplierId;

    private String supplierName;

    private BigDecimal totalPurchases;

    private BigDecimal totalPaid;

    private BigDecimal outstandingBalance;

    private List<SupplierLedgerInvoiceDTO> invoices;
}