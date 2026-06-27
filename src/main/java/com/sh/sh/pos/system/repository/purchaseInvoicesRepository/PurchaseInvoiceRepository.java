package com.sh.sh.pos.system.repository.purchaseInvoicesRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    Optional<PurchaseInvoice> findByInvoiceNumber(String invoiceNumber);
    List<PurchaseInvoice> findByPurchaseOrderId(Long purchaseOrderId);
    List<PurchaseInvoice> findBySupplierId(Long supplierId);
}