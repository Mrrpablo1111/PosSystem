package com.sh.sh.pos.system.service.serviceImpl.purchaseInvoicesServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.PaymentStatus;
import com.sh.sh.pos.system.domain.PurchaseInvoiceStatus;
import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.domain.ReferenceType;
import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.mapper.purchaseInvoicesMapper.PurchaseInvoiceMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.StockMovement;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.purchaseInvoiceItems.PurchaseInvoiceItem;
import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesOrderItem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.purchaseInvoiceItemsDTO.PurchaseInvoiceItemDTO;
import com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO.PurchaseInvoiceDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.StockMovementRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.purchaseInvoicesRepository.PurchaseInvoiceRepository;
import com.sh.sh.pos.system.repository.purchasesOrderRepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.repository.suppliersRepository.SupplierRepository;
import com.sh.sh.pos.system.service.purchaseInvoicesService.PurchaseInvoiceService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceServiceImpl implements PurchaseInvoiceService {

    private final PurchaseInvoiceRepository invoiceRepository;
    private final SupplierRepository        supplierRepository;
    private final BranchRepository          branchRepository;
    private final ProductRepository         productRepository;
    private final ProductVariantRepository  variantRepository;
    private final ProductBatchRepository    batchRepository;
    private final StockMovementRepository   stockMovementRepository;
    private final StoreRepository           storeRepository;
    private final PurchaseOrderRepository   purchaseOrderRepository;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PurchaseInvoiceDTO create(PurchaseInvoiceDTO dto) {

        Store    store    = findStore(dto.getStoreId());
        Supplier supplier = findSupplier(dto.getSupplierId());
        Branch   branch   = findBranch(dto.getBranchId());

        // Validate branch belongs to store
        if (!branch.getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException(
                    "Branch " + branch.getId() +
                    " does not belong to store " + store.getId());
        }

        // Optionally link to a PO
        PurchaseOrder purchaseOrder = null;
        if (dto.getPurchaseOrderId() != null) {
            purchaseOrder = purchaseOrderRepository
                    .findById(dto.getPurchaseOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Purchase order not found: " + dto.getPurchaseOrderId()));
        }

        PurchaseInvoice invoice = PurchaseInvoiceMapper.toEntity(dto, store, supplier, branch);
        invoice.setPurchaseOrder(purchaseOrder);
        invoice.setInvoiceNumber(generateInvoiceNumber());

        List<PurchaseInvoiceItem> items = buildInvoiceItems(dto, invoice);
        invoice.setItems(items);
        invoice.setSubtotal(calculateSubtotal(items));
        invoice.setTotalAmount(calculateTotal(invoice));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(invoice.getTotalAmount());
        invoice.setPaymentStatus(PaymentStatus.UNPAID);

        return PurchaseInvoiceMapper.toDTO(invoiceRepository.save(invoice));
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    public PurchaseInvoiceDTO getById(Long id) {
        return PurchaseInvoiceMapper.toDTO(
                invoiceRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Invoice not found: " + id)));
    }

    @Override
    public List<PurchaseInvoiceDTO> getAll() {
        return invoiceRepository.findAll()
                .stream()
                .map(PurchaseInvoiceMapper::toDTO)
                .toList();
    }

    // ── Post (commit stock) ───────────────────────────────────────────────

    @Override
    @Transactional
    public void post(Long invoiceId) {

        PurchaseInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Invoice not found: " + invoiceId));

        if (invoice.getStatus() == PurchaseInvoiceStatus.POSTED) {
            throw new IllegalStateException(
                    "Invoice already posted: " + invoiceId);
        }

        // Create batch and record stock movement per line item
        for (PurchaseInvoiceItem item : invoice.getItems()) {
            ProductBatch batch = createBatch(item, invoice);
            recordStockMovement(item, invoice, batch);
        }

        invoice.setStatus(PurchaseInvoiceStatus.POSTED);
        invoiceRepository.save(invoice);

        // Sync the linked PO status based on received quantities
        if (invoice.getPurchaseOrder() != null) {
            syncPurchaseOrderStatus(invoice.getPurchaseOrder());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private List<PurchaseInvoiceItem> buildInvoiceItems(
            PurchaseInvoiceDTO dto, PurchaseInvoice invoice) {
        return dto.getItems().stream()
                .map(itemDto -> toInvoiceItem(itemDto, invoice))
                .toList();
    }

    private PurchaseInvoiceItem toInvoiceItem(
            PurchaseInvoiceItemDTO itemDto, PurchaseInvoice invoice) {

        Product        product = findProduct(itemDto.getProductId());
        ProductVariant variant = itemDto.getVariantId() != null
                ? findVariant(itemDto.getVariantId())
                : null;

        BigDecimal totalCost = itemDto.getUnitCost()
                .multiply(BigDecimal.valueOf(itemDto.getQuantity()));

        return PurchaseInvoiceItem.builder()
                .purchaseInvoice(invoice)
                .product(product)
                .variant(variant)
                .quantity(itemDto.getQuantity())
                .unitCost(itemDto.getUnitCost())
                .totalCost(totalCost)
                .batchNo(itemDto.getBatchNo())
                .expiryDate(itemDto.getExpiryDate())
                .build();
    }

    /**
     * Merges into an existing batch if same product + branch + batchNo,
     * otherwise creates a new batch.
     */
    private ProductBatch createBatch(
            PurchaseInvoiceItem item, PurchaseInvoice invoice) {

        return batchRepository
                .findByProductIdAndBranchIdAndBatchNo(
                        item.getProduct().getId(),
                        invoice.getBranch().getId(),
                        item.getBatchNo())
                .map(existing -> {
                    // Same lot — add to existing quantities
                    existing.setQuantity(
                            existing.getQuantity() + item.getQuantity());
                    existing.setRemainingQuantity(
                            existing.getRemainingQuantity() + item.getQuantity());
                    return batchRepository.save(existing);
                })
                .orElseGet(() ->
                    // New lot — create fresh batch
                    batchRepository.save(
                            ProductBatch.builder()
                                    .product(item.getProduct())
                                    .variant(item.getVariant())
                                    .branch(invoice.getBranch())
                                    .supplier(invoice.getSupplier())
                                    .batchNo(item.getBatchNo())
                                    .quantity(item.getQuantity())
                                    .remainingQuantity(item.getQuantity())
                                    .purchasePrice(item.getUnitCost())
                                    .expiryDate(item.getExpiryDate())
                                    .build()));
    }

    private void recordStockMovement(
            PurchaseInvoiceItem item,
            PurchaseInvoice invoice,
            ProductBatch batch) {

        stockMovementRepository.save(
                StockMovement.builder()
                        .branch(invoice.getBranch())
                        .product(item.getProduct())
                        .variant(item.getVariant())
                        .batch(batch)
                        .movementType(StockMovementType.IN)
                        .quantity(item.getQuantity())
                        .unitCost(item.getUnitCost())
                        .totalCost(item.getTotalCost())
                        .referenceType(ReferenceType.PURCHASE_INVOICE)
                        .referenceId(invoice.getId())
                        .build());
    }

    /**
     * Syncs PO status after an invoice is posted.
     *
     * FIX 1: uses getRequestedQuantity() — not the old getQuantity()
     * FIX 2: uses PARTIALLY_RECEIVED and RECEIVED — not PARTIALLY_INVOICED / COMPLETED
     *
     * Transitions:
     *   receivedQty == 0              → APPROVED          (nothing posted yet)
     *   0 < receivedQty < orderedQty  → PARTIALLY_RECEIVED
     *   receivedQty >= orderedQty     → RECEIVED
     */
    private void syncPurchaseOrderStatus(PurchaseOrder purchaseOrder) {

        // FIX: was PurchaseOrderItem::getQuantity — renamed to requestedQuantity
        int orderedQty = purchaseOrder.getItems().stream()
                .mapToInt(PurchaseOrderItem::getRequestedQuantity)
                .sum();

        int receivedQty = invoiceRepository
                .findByPurchaseOrderId(purchaseOrder.getId())
                .stream()
                .filter(inv -> inv.getStatus() == PurchaseInvoiceStatus.POSTED)
                .flatMap(inv -> inv.getItems().stream())
                .mapToInt(PurchaseInvoiceItem::getQuantity)
                .sum();

        if (receivedQty == 0) {
            purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        } else if (receivedQty < orderedQty) {
            // FIX: was PARTIALLY_INVOICED — does not exist in enum
            purchaseOrder.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        } else {
            // FIX: was COMPLETED — does not exist in enum
            purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        }

        purchaseOrderRepository.save(purchaseOrder);
    }

    private BigDecimal calculateSubtotal(List<PurchaseInvoiceItem> items) {
        return items.stream()
                .map(PurchaseInvoiceItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotal(PurchaseInvoice invoice) {
        BigDecimal subtotal = orZero(invoice.getSubtotal());
        BigDecimal discount = orZero(invoice.getDiscount());
        BigDecimal tax      = orZero(invoice.getTax());
        return subtotal.subtract(discount).add(tax);
    }

    private String generateInvoiceNumber() {
        return "PINV-" + System.currentTimeMillis();
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    // ── Finders ───────────────────────────────────────────────────────────

    private Store findStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Store not found: " + id));
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found: " + id));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Branch not found: " + id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found: " + id));
    }

    private ProductVariant findVariant(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Variant not found: " + id));
    }
}