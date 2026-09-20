package com.sh.sh.pos.system.validator;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.enums.GoodsReceiptStatus;
import com.sh.sh.pos.system.exception.UserException;


import com.sh.sh.pos.system.model.goodreceipt.GoodsReceipt;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptItemCreateDTO;

/**
 * All business validation for the Goods Receipt module, separated from the
 * service so ServiceImpl reads as pure workflow.
 *
 * Throws UserException (→ 400) on every violation, matching the project's
 * existing exception convention.
 */
@Component
public class GoodsReceiptValidator {

    /**
     * ADAPT: literals must match your PurchaseOrderStatus enum names.
     * A PO can receive goods while approved, sent, or already partially received.
     */
    private static final Set<String> RECEIVABLE_PO_STATUSES =
        Set.of("APPROVED", "SENT", "PARTIALLY_RECEIVED");

    private static final Set<GoodsReceiptStatus> EDITABLE_STATUSES =
        Set.of(GoodsReceiptStatus.DRAFT);

    private static final Set<GoodsReceiptStatus> COMPLETABLE_STATUSES =
        Set.of(GoodsReceiptStatus.DRAFT, GoodsReceiptStatus.RECEIVING);

    private static final Set<GoodsReceiptStatus> CANCELLABLE_STATUSES =
        Set.of(GoodsReceiptStatus.DRAFT, GoodsReceiptStatus.RECEIVING);

    // ── Header-level ──────────────────────────────────────────────────────

    public void validatePurchaseOrderReceivable(PurchaseOrder po) {
        String status = String.valueOf(po.getStatus());
        if (!RECEIVABLE_PO_STATUSES.contains(status)) {
            throw new UserException(
                "Purchase order " + po.getPoNumber() + " is " + status
                + " and cannot receive goods. Only approved or sent orders can be received.");
        }
    }

    public void validateSupplierMatches(PurchaseOrder po, Long supplierId) {
        if (!Objects.equals(po.getSupplier().getId(), supplierId)) {
            throw new UserException("Supplier does not match the purchase order's supplier.");
        }
    }

    // ── Item-level ────────────────────────────────────────────────────────

    /**
     * Validates every receipt line against the PO:
     *  - the PO item belongs to this PO
     *  - no duplicate PO item lines in one receipt
     *  - quantities non-negative, received ≤ remaining
     *  - accepted + damaged + rejected == received
     *  - batch number required when the product is batch-tracked / has expiry
     *  - expiry date not in the past, manufacture date not after expiry
     */
    public void validateItems(PurchaseOrder po, List<GoodsReceiptItemCreateDTO> items) {
        Map<Long, PurchaseOrderItem> poItems = po.getItems().stream()
            .collect(Collectors.toMap(PurchaseOrderItem::getId, Function.identity()));

        Set<Long> seen = new HashSet<>();

        for (GoodsReceiptItemCreateDTO dto : items) {
            PurchaseOrderItem poItem = poItems.get(dto.getPurchaseOrderItemId());
            if (poItem == null) {
                throw new UserException(
                    "Item " + dto.getPurchaseOrderItemId()
                    + " does not belong to purchase order " + po.getPoNumber() + ".");
            }
            if (!seen.add(dto.getPurchaseOrderItemId())) {
                throw new UserException(
                    "Duplicate line for purchase order item " + dto.getPurchaseOrderItemId() + ".");
            }
            validateQuantities(poItem, dto);
            validateBatchTracking(dto);
        }
    }

    private void validateQuantities(PurchaseOrderItem poItem, GoodsReceiptItemCreateDTO dto) {
        int received = nz(dto.getReceivedQuantity());
        int accepted = nz(dto.getAcceptedQuantity());
        int damaged  = nz(dto.getDamagedQuantity());
        int rejected = nz(dto.getRejectedQuantity());
        String product = productLabel(poItem);

        if (received <= 0) {
            throw new UserException("Received quantity for " + product + " must be greater than zero.");
        }
        if (accepted < 0 || damaged < 0 || rejected < 0) {
            throw new UserException("Quantities for " + product + " cannot be negative.");
        }

        int remaining = nz(poItem.getRemainingQuantity());
        if (received > remaining) {
            throw new UserException(
                "Cannot receive " + received + " of " + product
                + " — only " + remaining + " remaining on the purchase order.");
        }

        if (accepted + damaged + rejected != received) {
            throw new UserException(
                "For " + product + ": accepted (" + accepted + ") + damaged (" + damaged
                + ") + rejected (" + rejected + ") must equal received (" + received + ").");
        }
    }

    private void validateBatchTracking(GoodsReceiptItemCreateDTO dto) {
        boolean hasDates = dto.getExpiryDate() != null || dto.getManufactureDate() != null;

        // A line carrying expiry/manufacture info must be traceable to a batch
        if (hasDates && isBlank(dto.getBatchNumber())) {
            throw new UserException(
                "Batch number is required when an expiry or manufacture date is provided.");
        }
        if (dto.getExpiryDate() != null && dto.getExpiryDate().isBefore(LocalDate.now())) {
            throw new UserException(
                "Cannot receive expired stock — expiry date " + dto.getExpiryDate() + " is in the past.");
        }
        if (dto.getExpiryDate() != null && dto.getManufactureDate() != null
                && dto.getManufactureDate().isAfter(dto.getExpiryDate())) {
            throw new UserException("Manufacture date cannot be after expiry date.");
        }
    }

    // ── Status transitions ────────────────────────────────────────────────

    public void validateEditable(GoodsReceipt grn) {
        if (!EDITABLE_STATUSES.contains(grn.getStatus())) {
            throw new UserException(
                "Goods receipt " + grn.getGrnNumber() + " is " + grn.getStatus()
                + " and can no longer be modified. Only DRAFT receipts are editable.");
        }
    }

    public void validateCanStartReceiving(GoodsReceipt grn) {
        if (grn.getStatus() != GoodsReceiptStatus.DRAFT) {
            throw new UserException(
                "Only DRAFT receipts can move to RECEIVING. "
                + grn.getGrnNumber() + " is " + grn.getStatus() + ".");
        }
    }

    public void validateCanComplete(GoodsReceipt grn) {
        if (!COMPLETABLE_STATUSES.contains(grn.getStatus())) {
            throw new UserException(
                "Goods receipt " + grn.getGrnNumber() + " is " + grn.getStatus()
                + " and cannot be completed.");
        }
    }

    public void validateCanCancel(GoodsReceipt grn) {
        if (!CANCELLABLE_STATUSES.contains(grn.getStatus())) {
            throw new UserException(
                "Goods receipt " + grn.getGrnNumber() + " is " + grn.getStatus()
                + " and cannot be cancelled. Completed receipts must be reversed with a return.");
        }
    }

    /**
     * Re-checks remaining quantities at COMPLETE time.
     * Two drafts created against the same PO lines could otherwise both complete
     * and over-receive — the second must fail here.
     */
    public void validateRemainingAtCompletion(GoodsReceipt grn) {
        grn.getItems().forEach(item -> {
            int remaining = nz(item.getPurchaseOrderItem().getRemainingQuantity());
            if (nz(item.getReceivedQuantity()) > remaining) {
                throw new UserException(
                    "Cannot complete " + grn.getGrnNumber() + ": line for "
                    + productLabel(item.getPurchaseOrderItem()) + " receives "
                    + item.getReceivedQuantity() + " but only " + remaining
                    + " remain on the purchase order (another receipt may have been completed first).");
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String productLabel(PurchaseOrderItem poItem) {
        // ADAPT: "getName()" must match your Product entity's display field
        try {
            return poItem.getProduct() != null ? poItem.getProduct().getName()
                 : "PO item #" + poItem.getId();
        } catch (Exception e) {
            return "PO item #" + poItem.getId();
        }
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
