package com.sh.sh.pos.system.mapper.suppliersMapper;


import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;
import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierLedgerInvoiceDTO;

public class SupplierLedgerMapper {

    public static SupplierLedgerInvoiceDTO toInvoiceDTO(
            PurchaseInvoice invoice) {

        return SupplierLedgerInvoiceDTO.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .balanceAmount(invoice.getBalanceAmount())
                .paymentStatus(invoice.getPaymentStatus().name())
                .build();
    }
}