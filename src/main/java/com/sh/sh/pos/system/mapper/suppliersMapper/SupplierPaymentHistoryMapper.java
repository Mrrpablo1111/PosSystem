package com.sh.sh.pos.system.mapper.suppliersmapper;



import com.sh.sh.pos.system.model.suppliers.SupplierPayment;
import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierPaymentHistoryDTO;

public class SupplierPaymentHistoryMapper {
    private SupplierPaymentHistoryMapper() {}
    public static SupplierPaymentHistoryDTO toDTO(
            SupplierPayment payment) {

        return SupplierPaymentHistoryDTO.builder()
                .paymentId(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .invoiceId(payment.getPurchaseInvoice().getId())
                .invoiceNumber(
                        payment.getPurchaseInvoice().getInvoiceNumber())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod().name())
                .amountPaid(payment.getAmountPaid())
                .note(payment.getNote())
                .build();
    }
}