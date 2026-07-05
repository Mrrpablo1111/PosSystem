package com.sh.sh.pos.system.repository.purchaseinvoiceitemsrepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.purchaseinvoiceitems.PurchaseInvoiceItem;

public interface PurchaseInvoiceItemRepository extends JpaRepository<PurchaseInvoiceItem, Long> {}