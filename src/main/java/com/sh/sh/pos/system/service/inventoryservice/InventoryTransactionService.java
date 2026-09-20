package com.sh.sh.pos.system.service.inventoryservice;

import java.time.LocalDate;
import java.util.List;

import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.inventorytransaction.InventoryTransaction;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.inventorytransactiondto.InventoryTransactionDTO;

/**
 * Two layers, same split rationale as InventoryService:
 *
 *  1. WRITE — recordTransaction(). Entity in, entity out. Called only by
 *     the business modules that cause a transaction (GoodsReceiptService,
 *     future Sale/Adjustment/Transfer services) inside their own
 *     @Transactional method — never over HTTP. This is what keeps the
 *     ledger trustworthy, per InventoryTransactionController's own
 *     read-only design comment.
 *
 *  2. READ — everything InventoryTransactionController calls. DTO out,
 *     paginated where the controller expects pagination.
 *
 * No update/delete method exists on either layer — the ledger is
 * append-only by omission, not by convention.
 */
public interface InventoryTransactionService {

    // ── Write (internal callers only) ───────────────────────────────────
    InventoryTransaction recordTransaction(
            Inventory inventory,
            int beforeQuantity,
            int afterQuantity,
            String referenceType,
            Long referenceId,
            User performedBy,
            String reason);

    // ── Read (InventoryTransactionController) ───────────────────────────
    List<InventoryTransactionDTO> getAll(int page, int size);

    InventoryTransactionDTO getById(Long id);

    List<InventoryTransactionDTO> getByProduct(Long productId);

    List<InventoryTransactionDTO> getByWarehouse(Long warehouseId);

    List<InventoryTransactionDTO> getByProductAndWarehouse(Long productId, Long warehouseId);

    /** e.g. referenceType="GOODS_RECEIPT", referenceId=42 — the full
     *  movement trail for one source document. */
    List<InventoryTransactionDTO> getByReference(String referenceType, Long referenceId);

    List<InventoryTransactionDTO> getByDateRange(LocalDate from, LocalDate to);

    /** Sum of all signed quantities for one item — an audit check that
     *  should always equal the Inventory table's current quantity. If it
     *  doesn't, something wrote to Inventory without going through the
     *  transaction ledger (a bug worth finding). */
    Long getNetQuantity(Long productId, Long variantId, Long warehouseId);
}
