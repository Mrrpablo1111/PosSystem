package com.sh.sh.pos.system.mapper.suppliersMapper;



import com.sh.sh.pos.system.model.suppliers.SupplierPayment;
import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierPaymentHistoryDTO;

public class SupplierPaymentHistoryMapper {

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