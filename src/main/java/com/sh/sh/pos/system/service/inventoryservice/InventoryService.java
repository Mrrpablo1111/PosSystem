package com.sh.sh.pos.system.service.inventoryservice;

import java.util.List;
import java.util.Optional;

import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.payload.dto.inventorydto.InventoryDTO;

/**
 * Two layers on one interface, deliberately:
 *
 *  1. CRUD (createInventory/updateInventory/deleteInventory/getX) — DTO in,
 *     DTO out. Used by InventoryController for admin-facing management
 *     (e.g. manually creating an inventory row, editing reorder level via
 *     a form). Matches your existing controller exactly.
 *
 *  2. Quantity mutators (increaseStock/decreaseStock/reserveStock/...) —
 *     entity in, entity out. Used by GoodsReceiptService, TransferService,
 *     InventoryAdjustmentService — internal business-logic callers that
 *     need the real entity (to read .getBranch()/.getProduct() for
 *     movement/transaction logging) and must never round-trip through a
 *     DTO just to get it back as an entity again.
 *
 * This service must never import PurchaseOrder, GoodsReceipt, or any
 * document-level entity — callers pass a plain int delta, never a
 * reference to the source document.
 */
public interface InventoryService {

    // ── CRUD (existing contract — unchanged) ────────────────────────────
    InventoryDTO createInventory(InventoryDTO dto);
    InventoryDTO updateInventory(Long id, InventoryDTO dto);
    void deleteInventory(Long id);
    InventoryDTO getInventoryById(Long id);
    List<InventoryDTO> getInventoryByProductId(Long productId);
    List<InventoryDTO> getInventoryByBranch(Long branchId);
    List<InventoryDTO> getInventoryByWarehouse(Long warehouseId);
    List<InventoryDTO> getLowStockByBranch(Long branchId);

    // ── Quantity mutators (new — entity-based, internal callers) ────────
    Optional<Inventory> findInventory(Long productId, Long variantId, Long warehouseId);
    Inventory findOrCreateInventory(Long productId, Long variantId, Long warehouseId, Long branchId);

    Inventory increaseStock(Long inventoryId, int quantity);
    Inventory decreaseStock(Long inventoryId, int quantity);
    Inventory reserveStock(Long inventoryId, int quantity);
    Inventory releaseReservedStock(Long inventoryId, int quantity);
    Inventory updateReorderLevel(Long inventoryId, int reorderLevel);
    int getAvailableQuantity(Long inventoryId);
}
