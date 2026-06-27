package com.sh.sh.pos.system.mapper.purchasesOrderMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.mapper.currenciesMapper.CurrencyMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesOrderItem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO.PurchaseInvoiceSummaryDTO;
import com.sh.sh.pos.system.payload.dto.purchasesOrderDTO.PurchaseOrderDTO;
import com.sh.sh.pos.system.payload.dto.purchasesOrderItemDTO.PurchaseOrderItemDTO;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest.PurchaseOrderItemRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PurchaseOrderMapper {

    private final CurrencyMapper currencyMapper;

    // ── Entity → DTO ──────────────────────────────────────────────────────

    public PurchaseOrderDTO toDTO(PurchaseOrder order) {

        boolean fullyReceived = order.getItems() != null
                && !order.getItems().isEmpty()
                && order.getItems().stream().allMatch(PurchaseOrderItem::isFullyReceived);

        boolean overdue = order.getExpectedDelivery() != null
                && order.getExpectedDelivery().isBefore(LocalDate.now())
                && order.getReceivedDate() == null;

        return PurchaseOrderDTO.builder()
                .id(order.getId())
                .poNumber(order.getPoNumber())
                .referenceNo(order.getReferenceNo())

                .supplierId(order.getSupplier() != null ? order.getSupplier().getId()   : null)
                .supplierName(order.getSupplier() != null ? order.getSupplier().getName() : null)

                .branchId(order.getBranch() != null ? order.getBranch().getId()   : null)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : null)

                // null-safe — currency may be null on old records saved before
                // the currency column was added
                .currency(order.getCurrency() != null
                        ? currencyMapper.toDTO(order.getCurrency())
                        : null)

                .status(order.getStatus() != null ? order.getStatus().name() : null)

                .orderDate(order.getOrderDate())
                .expectedDelivery(order.getExpectedDelivery())
                .receivedDate(order.getReceivedDate())
                .paymentDueDate(order.getPaymentDueDate())

                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .totalAmount(order.getTotalAmount())

                .paymentTermDays(order.getPaymentTermDays())
                .carrierId(order.getCarrierId())
                .trackingNo(order.getTrackingNo())
                .notes(order.getNotes())

                .fullyReceived(fullyReceived)
                .overdue(overdue)

                .sentToSupplierAt(order.getSentToSupplierAt())
                .receiptConfirmationSentAt(order.getReceiptConfirmationSentAt())
                .createdAt(order.getCreatedAt())

                .items(order.getItems() == null
                        ? List.of()
                        : order.getItems().stream()
                                .map(this::toItemDTO)
                                .collect(Collectors.toList()))

                .invoices(order.getPurchaseInvoices() == null
                        ? List.of()
                        : order.getPurchaseInvoices().stream()
                                .map(invoice -> PurchaseInvoiceSummaryDTO.builder()
                                        .id(invoice.getId())
                                        .invoiceNumber(invoice.getInvoiceNumber())
                                        .status(invoice.getStatus().name())
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }

    // ── Request → Entity ──────────────────────────────────────────────────

    public PurchaseOrder toEntity(
            CreatePurchaseOrderRequest req,
            Supplier supplier,
            Branch branch,
            Currency currency,        // resolved by service — never null here
            List<PurchaseOrderItem> items) {

        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .branch(branch)
                .currency(currency)
                .orderDate(req.getOrderDate())
                .expectedDelivery(req.getExpectedDelivery())
                .referenceNo(req.getReferenceNo())
                .paymentTermDays(req.getPaymentTermDays())
                .discount(orZero(req.getDiscount()))
                .tax(orZero(req.getTax()))
                .shippingCost(orZero(req.getShippingCost()))
                .subtotal(orZero(req.getSubtotal()))
                .totalAmount(orZero(req.getTotalAmount()))
                .carrierId(req.getCarrierId())
                .trackingNo(req.getTrackingNo())
                .notes(req.getNotes())
                .build();

        if (items != null) {
            items.forEach(it -> it.setPurchaseOrder(po));
            po.setItems(items);
        }

        return po;
    }

    // ── Item request → entity ─────────────────────────────────────────────

    public PurchaseOrderItem toItemEntity(
            PurchaseOrderItemRequest req,
            Product product,
            ProductVariant variant) {

        BigDecimal discPct = req.getDiscountPct() != null
                ? req.getDiscountPct() : BigDecimal.ZERO;

        BigDecimal base  = req.getUnitCost()
                .multiply(BigDecimal.valueOf(req.getRequestedQuantity()));

        BigDecimal total = base.subtract(
                base.multiply(discPct).divide(BigDecimal.valueOf(100)));

        return PurchaseOrderItem.builder()
                .product(product)
                .variant(variant)
                .requestedQuantity(req.getRequestedQuantity())
                .receivedQuantity(0)
                .remainingQuantity(req.getRequestedQuantity())
                .unitCost(req.getUnitCost())
                .discountPct(discPct)
                .totalCost(total)
                .note(req.getNote())
                .build();
    }

    // ── Item entity → DTO ─────────────────────────────────────────────────

    public PurchaseOrderItemDTO toItemDTO(PurchaseOrderItem item) {
        return PurchaseOrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId()   : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .variantId(item.getVariant() != null ? item.getVariant().getId()   : null)
                .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                .requestedQuantity(item.getRequestedQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .remainingQuantity(item.getRemainingQuantity())
                .unitCost(item.getUnitCost())
                .discountPct(item.getDiscountPct())
                .totalCost(item.getTotalCost())
                .note(item.getNote())
                .fullyReceived(item.isFullyReceived())
                .build();
    }

    // ── Util ──────────────────────────────────────────────────────────────

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}