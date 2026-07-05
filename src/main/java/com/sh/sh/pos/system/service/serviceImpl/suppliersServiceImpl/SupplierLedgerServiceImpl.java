package com.sh.sh.pos.system.service.serviceimpl.suppliersserviceimpl;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.suppliersmapper.SupplierLedgerMapper;
import com.sh.sh.pos.system.model.purchaseinvoices.PurchaseInvoice;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierLedgerDTO;
import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierLedgerInvoiceDTO;
import com.sh.sh.pos.system.repository.purchaseinvoicesrepository.PurchaseInvoiceRepository;
import com.sh.sh.pos.system.repository.suppliersrepository.SupplierRepository;
import com.sh.sh.pos.system.service.suppliersservice.SupplierLedgerService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierLedgerServiceImpl
        implements SupplierLedgerService {

    private final SupplierRepository supplierRepository;
    private final PurchaseInvoiceRepository invoiceRepository;

    @Override
    public SupplierLedgerDTO getLedger(Long supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Supplier not found"));

        List<PurchaseInvoice> invoices =
                invoiceRepository.findBySupplierId(supplierId);

        List<SupplierLedgerInvoiceDTO> invoiceDTOs =
                invoices.stream()
                        .map(SupplierLedgerMapper::toInvoiceDTO)
                        .toList();

        BigDecimal totalPurchases =
                invoices.stream()
                        .map(PurchaseInvoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid =
                invoices.stream()
                        .map(inv ->
                                inv.getPaidAmount() == null
                                        ? BigDecimal.ZERO
                                        : inv.getPaidAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingBalance =
                invoices.stream()
                        .map(inv ->
                                inv.getBalanceAmount() == null
                                        ? inv.getTotalAmount()
                                        : inv.getBalanceAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SupplierLedgerDTO.builder()
                .supplierId(supplier.getId())
                .supplierName(supplier.getName())
                .totalPurchases(totalPurchases)
                .totalPaid(totalPaid)
                .outstandingBalance(outstandingBalance)
                .invoices(invoiceDTOs)
                .build();
    }
}