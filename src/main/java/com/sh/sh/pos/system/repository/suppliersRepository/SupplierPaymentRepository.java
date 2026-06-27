package com.sh.sh.pos.system.repository.suppliersRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.suppliers.SupplierPayment;

public interface SupplierPaymentRepository
        extends JpaRepository<SupplierPayment, Long> {
    List<SupplierPayment> findBySupplierId(Long supplierId);

    List<SupplierPayment> findByPurchaseInvoiceId(Long invoiceId);
}