package com.sh.sh.pos.system.repository.purchaseInvoiceItemsRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.purchaseInvoiceItems.PurchaseInvoiceItem;

public interface PurchaseInvoiceItemRepository extends JpaRepository<PurchaseInvoiceItem, Long> {}