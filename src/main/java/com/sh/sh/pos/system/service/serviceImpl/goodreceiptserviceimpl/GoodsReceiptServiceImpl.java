package com.sh.sh.pos.system.service.serviceimpl.goodreceiptserviceimpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.enums.GoodsReceiptStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.mapper.goodreceiptmapper.GoodsReceiptMapper;
import com.sh.sh.pos.system.model.goodreceipt.GoodsReceipt;
import com.sh.sh.pos.system.model.goodreceipt.GoodsReceiptItem;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.inventorytransaction.InventoryTransaction;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptCreateDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptDashboardDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptItemCreateDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptResponseDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptUpdateDTO;
import com.sh.sh.pos.system.repository.goodreceiptrepository.GoodsReceiptRepository;
import com.sh.sh.pos.system.repository.inventoryrepository.InventoryRepository;
import com.sh.sh.pos.system.repository.inventorytransactionrepository.InventoryTransactionRepository;
import com.sh.sh.pos.system.repository.productbatchrepository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.purchaseorderitemrepository.PurchaseOrderItemRepository;
import com.sh.sh.pos.system.repository.purchasesorderrepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.repository.warehousesrepository.WarehouseRepository;
import com.sh.sh.pos.system.service.goodreceiptservice.GoodsReceiptService;
import com.sh.sh.pos.system.validator.GoodsReceiptValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoodsReceiptServiceImpl implements GoodsReceiptService {

    private static final String GRN_NOT_FOUND = "Goods receipt not found with id: ";
    private static final String PO_NOT_FOUND = "Purchase order not found with id: ";
    private static final String WAREHOUSE_NOT_FOUND = "Warehouse not found with id: ";

    private static final String REF_TYPE_GOODS_RECEIPT = "GOODS_RECEIPT";

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductBatchRepository productBatchRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final UserRepository userRepository;
    private final GoodsReceiptMapper mapper;
    private final GoodsReceiptValidator validator;

    // ═══════════════════════════════════════════════════════════════════════
    // Reads
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptResponseDTO> getAllGoodsReceipts() {
        return mapper.toResponseDtoList(goodsReceiptRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsReceiptResponseDTO> getGoodsReceiptsByPurchaseOrder(Long purchaseOrderId) {
        return mapper.toResponseDtoList(
                goodsReceiptRepository.findByPurchaseOrderIdOrderByCreatedAtDesc(purchaseOrderId));
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptDTO getGoodsReceiptById(Long id) {
        return mapper.toDto(findGoodsReceiptOrThrow(id));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Create / Update / Delete (DRAFT lifecycle — no stock impact)
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public GoodsReceiptDTO createGoodsReceipt(GoodsReceiptCreateDTO dto) {
        PurchaseOrder po = findPurchaseOrderOrThrow(dto.getPurchaseOrderId());
        Warehouse warehouse = findWarehouseOrThrow(dto.getWarehouseId());

        // Header validation
        validator.validatePurchaseOrderReceivable(po);
        validator.validateSupplierMatches(po, po.getSupplier().getId());
        // Item validation (belongs to PO, quantities, batch rules)
        validator.validateItems(po, dto.getItems());

        GoodsReceipt grn = GoodsReceipt.builder()
                .grnNumber(generateGrnNumber())
                .purchaseOrder(po)
                .supplier(po.getSupplier()) // derived — never trusted from client
                .branch(po.getBranch()) // derived — never trusted from client
                .warehouse(warehouse)
                .status(GoodsReceiptStatus.DRAFT)
                .receivedDate(dto.getReceivedDate() != null ? dto.getReceivedDate() : LocalDate.now())
                .receivedBy(currentUserOrNull())
                .note(dto.getNote())
                .build();

        dto.getItems().forEach(itemDto -> grn.addItem(buildItem(po, itemDto)));

        return mapper.toDto(goodsReceiptRepository.save(grn));
    }

    @Override
    @Transactional
    public GoodsReceiptDTO updateGoodsReceipt(Long id, GoodsReceiptUpdateDTO dto) {
        GoodsReceipt grn = findGoodsReceiptOrThrow(id);
        validator.validateEditable(grn);

        PurchaseOrder po = grn.getPurchaseOrder();
        validator.validatePurchaseOrderReceivable(po);
        validator.validateItems(po, dto.getItems());

        grn.setWarehouse(findWarehouseOrThrow(dto.getWarehouseId()));
        if (dto.getReceivedDate() != null)
            grn.setReceivedDate(dto.getReceivedDate());
        grn.setNote(dto.getNote());

        // Replace items wholesale (orphanRemoval deletes the old rows)
        grn.clearItems();
        dto.getItems().forEach(itemDto -> grn.addItem(buildItem(po, itemDto)));

        return mapper.toDto(goodsReceiptRepository.save(grn));
    }

    @Override
    @Transactional
    public void deleteGoodsReceipt(Long id) {
        GoodsReceipt grn = findGoodsReceiptOrThrow(id);
        validator.validateEditable(grn); // only DRAFT can be hard-deleted
        goodsReceiptRepository.delete(grn);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Status transitions
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public GoodsReceiptDTO startReceiving(Long id) {
        GoodsReceipt grn = findGoodsReceiptOrThrow(id);
        validator.validateCanStartReceiving(grn);
        grn.setStatus(GoodsReceiptStatus.RECEIVING);
        return mapper.toDto(goodsReceiptRepository.save(grn));
    }

    /**
     * The heart of the module. Everything below happens in ONE transaction —
     * if any step fails (batch creation, inventory update), the whole receipt
     * rolls back and the PO is untouched.
     */
    @Override
    @Transactional
    public GoodsReceiptDTO completeGoodsReceipt(Long id) {
        GoodsReceipt grn = findGoodsReceiptOrThrow(id);
        validator.validateCanComplete(grn);
        validator.validatePurchaseOrderReceivable(grn.getPurchaseOrder());
        // Guard against a concurrent receipt completing first and shrinking "remaining"
        validator.validateRemainingAtCompletion(grn);

        // 1. Update PO item quantities
        grn.getItems().forEach(this::applyToPurchaseOrderItem);

        // 2. Roll PO status up from its items
        updatePurchaseOrderStatus(grn.getPurchaseOrder());

        // 3. Post accepted stock to inventory (batch + transaction + quantity)
        grn.getItems().stream()
                .filter(item -> item.getAcceptedQuantity() != null && item.getAcceptedQuantity() > 0)
                .forEach(item -> postToInventory(grn, item));

        grn.setStatus(GoodsReceiptStatus.COMPLETED);
        return mapper.toDto(goodsReceiptRepository.save(grn));
    }

    @Override
    @Transactional
    public GoodsReceiptDTO cancelGoodsReceipt(Long id) {
        GoodsReceipt grn = findGoodsReceiptOrThrow(id);
        validator.validateCanCancel(grn);
        grn.setStatus(GoodsReceiptStatus.CANCELLED);
        return mapper.toDto(goodsReceiptRepository.save(grn));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Dashboard
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptDashboardDTO getDashboard() {
        LocalDate today = LocalDate.now();
        return GoodsReceiptDashboardDTO.builder()
                .todayReceipts(goodsReceiptRepository.countTodayReceipts(today))
                .pendingReceipts(goodsReceiptRepository.countByStatusIn(
                        List.of(GoodsReceiptStatus.DRAFT, GoodsReceiptStatus.RECEIVING)))
                .completedReceipts(goodsReceiptRepository.countByStatusIn(
                        List.of(GoodsReceiptStatus.COMPLETED)))
                .damagedItems(goodsReceiptRepository.sumDamagedItems())
                .recentReceipts(goodsReceiptRepository.findRecentReceipts(PageRequest.of(0, 10)))
                .pendingPurchaseOrders(goodsReceiptRepository.findPendingPurchaseOrders(PageRequest.of(0, 10)))
                .topSuppliers(goodsReceiptRepository.findTopSuppliers(PageRequest.of(0, 5)))
                .monthlyTrend(goodsReceiptRepository.findMonthlyTrend(today.minusMonths(11).withDayOfMonth(1)))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private — building
    // ═══════════════════════════════════════════════════════════════════════

    private GoodsReceiptItem buildItem(PurchaseOrder po, GoodsReceiptItemCreateDTO dto) {
        PurchaseOrderItem poItem = po.getItems().stream()
                .filter(i -> i.getId().equals(dto.getPurchaseOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase order item not found with id: " + dto.getPurchaseOrderItemId()));

        return GoodsReceiptItem.builder()
                .purchaseOrderItem(poItem)
                .product(poItem.getProduct())
                .productVariant(poItem.getVariant())
                .requestedQuantity(poItem.getRequestedQuantity()) // snapshot
                .receivedQuantity(dto.getReceivedQuantity())
                .acceptedQuantity(dto.getAcceptedQuantity())
                .damagedQuantity(nz(dto.getDamagedQuantity()))
                .rejectedQuantity(nz(dto.getRejectedQuantity()))
                .unitCost(poItem.getUnitCost()) // snapshot
                .batchNumber(dto.getBatchNumber())
                .expiryDate(dto.getExpiryDate())
                .manufactureDate(dto.getManufactureDate())
                .note(dto.getNote())
                .build();
    }

    /**
     * GRN-yyyyMMdd-0001 — sequence resets daily, unique constraint is the backstop.
     */
    private String generateGrnNumber() {
        String prefix = "GRN-" + LocalDate.now().toString().replace("-", "") + "-";
        long next = goodsReceiptRepository.countByGrnNumberStartingWith(prefix) + 1;
        String candidate = prefix + String.format("%04d", next);
        while (goodsReceiptRepository.existsByGrnNumber(candidate)) {
            candidate = prefix + String.format("%04d", ++next);
        }
        return candidate;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private — PO integration
    // ═══════════════════════════════════════════════════════════════════════

    private void applyToPurchaseOrderItem(GoodsReceiptItem item) {
        PurchaseOrderItem poItem = item.getPurchaseOrderItem();
        int newReceived = nz(poItem.getReceivedQuantity()) + nz(item.getReceivedQuantity());
        poItem.setReceivedQuantity(newReceived);
        poItem.setRemainingQuantity(Math.max(0, nz(poItem.getRequestedQuantity()) - newReceived));
        purchaseOrderItemRepository.save(poItem);
    }

    private void updatePurchaseOrderStatus(PurchaseOrder po) {
        boolean allReceived = po.getItems().stream()
                .allMatch(i -> nz(i.getRemainingQuantity()) == 0);

        if (allReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
            po.setReceivedDate(LocalDate.now());
        } else {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
        purchaseOrderRepository.save(po);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private — Inventory integration
    // Flow per accepted line: batch (if tracked) → ledger entry → stock +
    // ═══════════════════════════════════════════════════════════════════════

    private void postToInventory(GoodsReceipt grn, GoodsReceiptItem item) {
        int accepted = item.getAcceptedQuantity();

        // 1. Product batch — only for batch-tracked lines.
        //    ProductBatch.expiryDate is NOT NULL, so lines without an expiry
        //    (non-perishables) skip the batch and still post to inventory.
        if (isBatchTracked(item)) {
            upsertProductBatch(grn, item, accepted);
        }

        // 2. Inventory transaction — the audit trail entry
        InventoryTransaction txn = InventoryTransaction.builder()
                .product(item.getProduct())
                .productVariant(item.getProductVariant())
                .warehouse(grn.getWarehouse())
                .quantity(accepted) // positive = stock in
                .referenceType(REF_TYPE_GOODS_RECEIPT)
                .referenceId(grn.getId())
                .note("Goods receipt " + grn.getGrnNumber())
                .build();
        inventoryTransactionRepository.save(txn);

        // 3. Inventory quantity — find-or-create, then increment
        Inventory inventory = inventoryRepository
                .findByProductIdAndProductVariantIdAndWarehouseId(
                        item.getProduct().getId(),
                        item.getProductVariant() != null ? item.getProductVariant().getId() : null,
                        grn.getWarehouse().getId())
                .orElseGet(() -> Inventory.builder()
                        .branch(grn.getBranch()) // branch is NOT NULL — required for new rows
                        .product(item.getProduct())
                        .productVariant(item.getProductVariant())
                        .warehouse(grn.getWarehouse())
                        .quantity(0)
                        .build());

        inventory.setQuantity(nz(inventory.getQuantity()) + accepted);
        inventoryRepository.save(inventory);
    }

    private boolean isBatchTracked(GoodsReceiptItem item) {
        return item.getBatchNumber() != null && !item.getBatchNumber().isBlank()
                && item.getExpiryDate() != null;
    }

    /**
     * Same lot arriving in a second delivery INCREMENTS the existing batch —
     * a blind insert would violate the (warehouse, product, batch_no)
     * unique constraint.
     */
    private void upsertProductBatch(GoodsReceipt grn, GoodsReceiptItem item, int accepted) {
        productBatchRepository
                .findByWarehouseIdAndProductIdAndBatchNo(
                        grn.getWarehouse().getId(),
                        item.getProduct().getId(),
                        item.getBatchNumber())
                .ifPresentOrElse(
                        batch -> {
                            batch.setQuantity(nz(batch.getQuantity()) + accepted);
                            batch.setRemainingQuantity(nz(batch.getRemainingQuantity()) + accepted);
                            productBatchRepository.save(batch);
                        },
                        () -> productBatchRepository.save(ProductBatch.builder()
                                .branch(grn.getBranch()) // NOT NULL on product_batches too
                                .warehouse(grn.getWarehouse())
                                .product(item.getProduct())
                                .variant(item.getProductVariant())
                                .supplier(grn.getSupplier()) // traceability — who delivered this lot
                                .batchNo(item.getBatchNumber())
                                .expiryDate(item.getExpiryDate())
                                .manufactureDate(item.getManufactureDate())
                                .quantity(accepted)
                                .remainingQuantity(accepted)
                                .purchasePrice(item.getUnitCost())
                                .build()));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private — lookups
    // ═══════════════════════════════════════════════════════════════════════

    private GoodsReceipt findGoodsReceiptOrThrow(Long id) {
        return goodsReceiptRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GRN_NOT_FOUND + id));
    }

    private PurchaseOrder findPurchaseOrderOrThrow(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PO_NOT_FOUND + id));
    }

    private Warehouse findWarehouseOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(WAREHOUSE_NOT_FOUND + id));
    }

    /** Resolves the authenticated user; null when unauthenticated (e.g. system jobs). */
    private User currentUserOrNull() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(username); // repository returns User (or null) directly
        } catch (Exception e) {
            return null;
        }
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}