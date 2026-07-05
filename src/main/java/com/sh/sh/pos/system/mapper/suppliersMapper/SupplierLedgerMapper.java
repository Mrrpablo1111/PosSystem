package com.sh.sh.pos.system.mapper.suppliersmapper;


import com.sh.sh.pos.system.model.purchaseinvoices.PurchaseInvoice;
import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierLedgerInvoiceDTO;

public class SupplierLedgerMapper {
    private SupplierLedgerMapper() {}
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