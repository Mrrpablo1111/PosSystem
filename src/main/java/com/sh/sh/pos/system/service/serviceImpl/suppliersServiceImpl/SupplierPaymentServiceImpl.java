package com.sh.sh.pos.system.service.serviceImpl.suppliersServiceImpl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.PaymentStatus;
import com.sh.sh.pos.system.mapper.suppliersMapper.SupplierPaymentMapper;
import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.model.suppliers.SupplierPayment;
import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierPaymentDTO;
import com.sh.sh.pos.system.repository.purchaseInvoicesRepository.PurchaseInvoiceRepository;
import com.sh.sh.pos.system.repository.suppliersRepository.SupplierPaymentRepository;
import com.sh.sh.pos.system.service.suppliersService.SupplierPaymentService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierPaymentServiceImpl
        implements SupplierPaymentService {

    private final SupplierPaymentRepository paymentRepository;
    private final PurchaseInvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public SupplierPaymentDTO create(
            SupplierPaymentDTO dto) {

        PurchaseInvoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Invoice not found"));

        Supplier supplier = invoice.getSupplier();

        SupplierPayment payment = SupplierPaymentMapper.toEntity(
                dto,
                supplier,
                invoice);

        payment.setPaymentNumber(
                "PAY-" + System.currentTimeMillis());

        paymentRepository.save(payment);

        // update invoice payment info
        BigDecimal paid = (invoice.getPaidAmount() == null
                ? BigDecimal.ZERO
                : invoice.getPaidAmount())
                .add(dto.getAmountPaid());
        if (paid.compareTo(invoice.getTotalAmount()) > 0) {
            throw new IllegalArgumentException(
                    "Payment exceeds invoice amount");
        }
        BigDecimal balance = invoice.getTotalAmount()
                .subtract(paid);

        invoice.setPaidAmount(paid);
        invoice.setBalanceAmount(balance);

        if (paid.compareTo(BigDecimal.ZERO) == 0) {

            invoice.setPaymentStatus(PaymentStatus.UNPAID);

        } else if (balance.compareTo(BigDecimal.ZERO) == 0) {

            invoice.setPaymentStatus(PaymentStatus.PAID);

        } else {

            invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        return SupplierPaymentMapper.toDTO(payment);
    }
}