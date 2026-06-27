package com.sh.sh.pos.system.mapper.suppliersMapper;

import com.sh.sh.pos.system.domain.PaymentMethod;
import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;

import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.model.suppliers.SupplierPayment;
import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierPaymentDTO;

public class SupplierPaymentMapper {

    public static SupplierPayment toEntity(
            SupplierPaymentDTO dto,
            Supplier supplier,
            PurchaseInvoice invoice) {

        return SupplierPayment.builder()
                .supplier(supplier)
                .purchaseInvoice(invoice)
                .paymentDate(dto.getPaymentDate())
                .paymentMethod(
                        PaymentMethod.valueOf(dto.getPaymentMethod()))
                .amountPaid(dto.getAmountPaid())
                .note(dto.getNote())
                .build();
    }

    public static SupplierPaymentDTO toDTO(SupplierPayment payment) {

        return SupplierPaymentDTO.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .supplierId(payment.getSupplier().getId())
                .invoiceId(payment.getPurchaseInvoice().getId())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod().name())
                .amountPaid(payment.getAmountPaid())
                .note(payment.getNote())
                .build();
    }
}