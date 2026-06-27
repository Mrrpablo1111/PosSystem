package com.sh.sh.pos.system.service.serviceImpl.purchasesOrderServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.mapper.purchasesOrderMapper.PurchaseOrderMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesOrderItem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.purchasesOrderDTO.PurchaseOrderDTO;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest.PurchaseOrderItemRequest;

import com.sh.sh.pos.system.payload.request.ReceiveGoodsRequestDTO;

import com.sh.sh.pos.system.payload.request.UpdateTrackingRequestDTO;
import com.sh.sh.pos.system.repository.BranchRepository;

import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.currenciesRepository.CurrencyRepository;
import com.sh.sh.pos.system.repository.purchasesOrderRepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.repository.suppliersRepository.SupplierRepository;
import com.sh.sh.pos.system.service.EmailService;
import com.sh.sh.pos.system.service.purchasesOrderService.PurchaseOrderService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

        private final PurchaseOrderRepository purchaseOrderRepository;
        private final SupplierRepository supplierRepository;
        private final BranchRepository branchRepository;
        private final CurrencyRepository currencyRepository;
        private final ProductRepository productRepository;
        private final ProductVariantRepository variantRepository;
        private final PurchaseOrderMapper mapper;
        private final EmailService emailService;

        // ── Read ──────────────────────────────────────────────────────────────

        @Override
        public List<PurchaseOrderDTO> getAllByStore(Long storeId) {
                return purchaseOrderRepository.findByBranchStoreId(storeId)
                                .stream()
                                .map(mapper::toDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<PurchaseOrderDTO> getAll() {
                return purchaseOrderRepository.findAll()
                                .stream()
                                .map(mapper::toDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public PurchaseOrderDTO getById(Long id) {
                return mapper.toDTO(findOrThrow(id));
        }

        // ── Create ────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public PurchaseOrderDTO create(CreatePurchaseOrderRequest req) {

                Supplier supplier = findSupplier(req.getSupplierId());
                Branch branch = findBranch(req.getBranchId());

                // ── Currency resolution ───────────────────────────────────────────
                // FIX: currencyId is optional. If null or 0 → fall back to USD.
                // This prevents "Unable to find Currency with id 0" when the
                // frontend does not send a currency or sends 0.
                Currency currency = resolveCurrency(req.getCurrencyId());

                List<PurchaseOrderItem> items = req.getItems().stream()
                                .map(this::toOrderItem)
                                .collect(Collectors.toList());

                PurchaseOrder order = mapper.toEntity(req, supplier, branch, currency, items);

                items.forEach(it -> it.setPurchaseOrder(order));

                order.setPoNumber(generatePoNumber());
                order.setSubtotal(calculateSubtotal(items));
                order.setTotalAmount(calculateTotal(order));

                return mapper.toDTO(purchaseOrderRepository.save(order));
        }

        // ── Approve ───────────────────────────────────────────────────────────

        @Override
        @Transactional
        public PurchaseOrderDTO approve(Long id) {
                PurchaseOrder order = findOrThrow(id);

                if (order.getStatus() != PurchaseOrderStatus.PENDING &&
                                order.getStatus() != PurchaseOrderStatus.DRAFT) {
                        throw new IllegalStateException(
                                        "Cannot approve a PO with status: " + order.getStatus());
                }

                order.setStatus(PurchaseOrderStatus.APPROVED);
                return mapper.toDTO(purchaseOrderRepository.save(order));
        }

        // ── Cancel ────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public PurchaseOrderDTO cancel(Long id) {
                PurchaseOrder order = findOrThrow(id);

                if (order.getStatus() == PurchaseOrderStatus.RECEIVED ||
                                order.getStatus() == PurchaseOrderStatus.CLOSED) {
                        throw new IllegalStateException(
                                        "Cannot cancel a PO with status: " + order.getStatus());
                }

                order.setStatus(PurchaseOrderStatus.CANCELLED);
                return mapper.toDTO(purchaseOrderRepository.save(order));
        }

        // ── Receive goods ─────────────────────────────────────────────────────

        @Override
        @Transactional
        public PurchaseOrderDTO receiveGoods(ReceiveGoodsRequestDTO req) {
                PurchaseOrder order = findOrThrow(req.getPurchaseOrderId());

                if (order.getStatus() == PurchaseOrderStatus.CANCELLED ||
                                order.getStatus() == PurchaseOrderStatus.CLOSED) {
                        throw new IllegalStateException(
                                        "Cannot receive goods on a " + order.getStatus() + " order");
                }

                req.getItems().forEach(receipt -> {
                        PurchaseOrderItem item = order.getItems().stream()
                                        .filter(it -> it.getId().equals(receipt.getPurchaseOrderItemId()))
                                        .findFirst()
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Item not found on PO: " + receipt.getPurchaseOrderItemId()));

                        int newReceived = item.getReceivedQuantity() + receipt.getQuantityReceived();

                        if (newReceived > item.getRequestedQuantity()) {
                                throw new IllegalArgumentException(
                                                "Received qty exceeds requested for item: " + item.getId());
                        }

                        item.setReceivedQuantity(newReceived);
                        item.syncRemaining();

                        if (receipt.getNote() != null && !receipt.getNote().isBlank()) {
                                item.setNote(receipt.getNote());
                        }
                });

                boolean allReceived = order.getItems().stream()
                                .allMatch(PurchaseOrderItem::isFullyReceived);
                boolean anyReceived = order.getItems().stream()
                                .anyMatch(it -> it.getReceivedQuantity() > 0);

                if (allReceived) {
                        order.setStatus(PurchaseOrderStatus.RECEIVED);
                        order.setReceivedDate(
                                        req.getReceivedDate() != null
                                                        ? req.getReceivedDate()
                                                        : LocalDate.now());
                } else if (anyReceived) {
                        order.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
                }
                PurchaseOrder saved = purchaseOrderRepository.save(order);

                if(saved.getStatus() == PurchaseOrderStatus.RECEIVED){
                log.info("📧 Sending receipt confirmation email for PO={}", saved.getPoNumber());
                emailService.sendReceiptConfirmationEmail(saved);
                }
                return mapper.toDTO(saved);
        }

        // ── Send to supplier ──────────────────────────────────────────────────

        @Override
        @Transactional
        public PurchaseOrderDTO sendToSupplier(Long id) {
                PurchaseOrder order = findOrThrow(id);

                if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
                        throw new IllegalStateException(
                                        "Can only send a DRAFT PO to supplier, current status: "
                                                        + order.getStatus());
                }

                order.setStatus(PurchaseOrderStatus.PENDING);
                order.setSentToSupplierAt(LocalDateTime.now());
                PurchaseOrder saved = purchaseOrderRepository.save(order);

                emailService.sendPurchaseOrderEmail(saved); // ← this line must exist

                return mapper.toDTO(saved);
        }

        // ── Update tracking ───────────────────────────────────────────────────

        @Override
        @Transactional
        public PurchaseOrderDTO updateTracking(Long id, UpdateTrackingRequestDTO req) {
                PurchaseOrder order = findOrThrow(id);

                if (order.getStatus() == PurchaseOrderStatus.CLOSED ||
                                order.getStatus() == PurchaseOrderStatus.CANCELLED) {
                        throw new IllegalStateException(
                                        "Cannot update tracking on a " + order.getStatus() + " order");
                }

                String tracking = (req.getTrackingNo() != null && !req.getTrackingNo().isBlank())
                                ? req.getTrackingNo().trim()
                                : null;

                order.setTrackingNo(tracking);

                if (req.getCarrierId() != null) {
                        order.setCarrierId(req.getCarrierId());
                }

                return mapper.toDTO(purchaseOrderRepository.save(order));
        }

        // ── Private helpers ───────────────────────────────────────────────────

        /**
         * Resolves currency safely:
         * - null or 0 → falls back to the first active currency (usually USD)
         * - valid id → loads and validates it is active
         * - not found → throws a clear EntityNotFoundException
         */
        private Currency resolveCurrency(Long currencyId) {
                // null or 0 means frontend did not send a currency → use default
                if (currencyId == null || currencyId == 0L) {
                        return currencyRepository.findByActiveTrue()
                                        .stream()
                                        .findFirst()
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "No active currency found. Please seed the currencies table first."));
                }

                return currencyRepository.findById(currencyId)
                                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Currency not found or inactive: " + currencyId));
        }

        private PurchaseOrderItem toOrderItem(PurchaseOrderItemRequest req) {
                Product product = findProduct(req.getProductId());

                ProductVariant variant = req.getVariantId() != null
                                ? findVariant(req.getVariantId())
                                : null;

                BigDecimal discPct = req.getDiscountPct() != null
                                ? req.getDiscountPct()
                                : BigDecimal.ZERO;

                BigDecimal base = req.getUnitCost()
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

        private BigDecimal calculateSubtotal(List<PurchaseOrderItem> items) {
                return items.stream()
                                .map(PurchaseOrderItem::getTotalCost)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal calculateTotal(PurchaseOrder order) {
                BigDecimal subtotal = orZero(order.getSubtotal());
                BigDecimal discount = orZero(order.getDiscount());
                BigDecimal tax = orZero(order.getTax());
                BigDecimal shipping = orZero(order.getShippingCost());
                return subtotal.subtract(discount).add(tax).add(shipping);
        }

        private String generatePoNumber() {
                return "PO-" + System.currentTimeMillis();
        }

        private PurchaseOrder findOrThrow(Long id) {
                return purchaseOrderRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Purchase Order not found: " + id));
        }

        private BigDecimal orZero(BigDecimal v) {
                return v != null ? v : BigDecimal.ZERO;
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