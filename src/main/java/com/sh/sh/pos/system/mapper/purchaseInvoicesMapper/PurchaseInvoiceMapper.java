package com.sh.sh.pos.system.mapper.purchaseInvoicesMapper;

import java.util.stream.Collectors;

import com.sh.sh.pos.system.domain.PurchaseInvoiceStatus;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.purchaseInvoiceItems.PurchaseInvoiceItem;
import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.purchaseInvoiceItemsDTO.PurchaseInvoiceItemDTO;
import com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO.PurchaseInvoiceDTO;

public class PurchaseInvoiceMapper {

        public static PurchaseInvoice toEntity(
                        PurchaseInvoiceDTO dto,
                        Store store,
                        Supplier supplier,
                        Branch branch) {

                return PurchaseInvoice.builder()
                                .store(store)
                                .supplier(supplier)
                                .branch(branch)
                                .invoiceDate(dto.getInvoiceDate())
                                .supplierInvoiceNo(dto.getSupplierInvoiceNo())
                                .status(PurchaseInvoiceStatus.DRAFT)
                                .discount(dto.getDiscount())
                                .tax(dto.getTax())
                                .build();
        }

        public static PurchaseInvoiceDTO toDTO(PurchaseInvoice invoice) {
                Store store = invoice.getStore();
                Supplier supplier = invoice.getSupplier();
                Branch branch = invoice.getBranch();
                return PurchaseInvoiceDTO.builder()
                                .id(invoice.getId())
                                .invoiceNumber(invoice.getInvoiceNumber())
                                .storeId(store != null ? store.getId() : null)
                                .supplierId(supplier != null ? supplier.getId() : null)
                                .branchId(branch != null ? branch.getId() : null)
                                .purchaseOrderId(
                                                invoice.getPurchaseOrder() != null
                                                                ? invoice.getPurchaseOrder().getId()
                                                                : null)
                                .supplierInvoiceNo(invoice.getSupplierInvoiceNo())
                                .invoiceDate(invoice.getInvoiceDate())
                                .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                                .subtotal(invoice.getSubtotal())
                                .discount(invoice.getDiscount())
                                .tax(invoice.getTax())
                                .totalAmount(invoice.getTotalAmount())
                                .paidAmount(invoice.getPaidAmount())
                                .balanceAmount(invoice.getBalanceAmount())
                                .paymentStatus(
                                                invoice.getPaymentStatus() != null
                                                                ? invoice.getPaymentStatus().name()
                                                                : null)
                                .items(
                                                invoice.getItems() == null
                                                                ? null
                                                                : invoice.getItems()
                                                                                .stream()
                                                                                .map(PurchaseInvoiceMapper::toItemDTO)
                                                                                .collect(Collectors.toList()))
                                .build();
        }

        private static PurchaseInvoiceItemDTO toItemDTO(
                        PurchaseInvoiceItem item) {

                return PurchaseInvoiceItemDTO.builder()
                                .productId(item.getProduct().getId())
                                .variantId(item.getVariant() != null
                                                ? item.getVariant().getId()
                                                : null)
                                .quantity(item.getQuantity())
                                .unitCost(item.getUnitCost())
                                .batchNo(item.getBatchNo())
                                .expiryDate(item.getExpiryDate())
                                .build();
        }
}