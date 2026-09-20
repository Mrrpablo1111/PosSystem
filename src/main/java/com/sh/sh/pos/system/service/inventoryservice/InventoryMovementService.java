package com.sh.sh.pos.system.service.inventoryservice;

import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.inventorymovement.InventoryMovement;

/**
 * Writes InventoryMovement rows ONLY — the "what happened" activity log
 * (as opposed to InventoryTransactionService's before/after ledger). Each
 * method sets MovementType internally so no caller ever constructs a raw
 * InventoryMovement or picks the enum value by hand.
 */
public interface InventoryMovementService {

    InventoryMovement recordGoodsReceipt(Inventory inventory, int quantity, Long referenceId);

    InventoryMovement recordSale(Inventory inventory, int quantity, Long referenceId);

    InventoryMovement recordAdjustment(Inventory inventory, int quantity, Long referenceId, String reason);

    InventoryMovement recordTransferIn(Inventory inventory, int quantity, Long referenceId);

    InventoryMovement recordTransferOut(Inventory inventory, int quantity, Long referenceId);

    InventoryMovement recordPurchaseReturn(Inventory inventory, int quantity, Long referenceId);

    InventoryMovement recordSalesReturn(Inventory inventory, int quantity, Long referenceId);

    InventoryMovement recordDamage(Inventory inventory, int quantity, Long referenceId, String reason);

    InventoryMovement recordOpeningStock(Inventory inventory, int quantity);
}
