package com.sh.sh.pos.system.service.serviceimpl.inventoryserviceimpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.MovementType;   // ADAPT: confirm this is the real package
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.inventorymovement.InventoryMovement;
import com.sh.sh.pos.system.repository.inventoryrepository.InventoryMovementRepository;
// ADAPT
import com.sh.sh.pos.system.service.inventoryservice.InventoryMovementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository movementRepository;

    // Per your InventoryMovementDTO shape: branch, product, type, quantity, note.
    // referenceId is accepted by every method here for interface consistency and
    // future-proofing, but only persisted if you add a referenceId column — see
    // the note at the bottom of this file.

    @Override
    @Transactional
    public InventoryMovement recordGoodsReceipt(Inventory inventory, int quantity, Long referenceId) {
        return save(inventory, MovementType.RECEIPT, quantity, "Goods receipt #" + referenceId);
    }

    @Override
    @Transactional
    public InventoryMovement recordSale(Inventory inventory, int quantity, Long referenceId) {
        return save(inventory, MovementType.SALE, -quantity, "Sale #" + referenceId);
    }

    @Override
    @Transactional
    public InventoryMovement recordAdjustment(Inventory inventory, int quantity, Long referenceId, String reason) {
        return save(inventory, MovementType.ADJUSTMENT, quantity,
                "Adjustment #" + referenceId + (reason != null ? " — " + reason : ""));
    }

    @Override
    @Transactional
    public InventoryMovement recordTransferIn(Inventory inventory, int quantity, Long referenceId) {
        return save(inventory, MovementType.TRANSFER_IN, quantity, "Transfer in #" + referenceId);
    }

    @Override
    @Transactional
    public InventoryMovement recordTransferOut(Inventory inventory, int quantity, Long referenceId) {
        return save(inventory, MovementType.TRANSFER_OUT, -quantity, "Transfer out #" + referenceId);
    }

    @Override
    @Transactional
    public InventoryMovement recordPurchaseReturn(Inventory inventory, int quantity, Long referenceId) {
        return save(inventory, MovementType.PURCHASE_RETURN, -quantity, "Purchase return #" + referenceId);
    }

    @Override
    @Transactional
    public InventoryMovement recordSalesReturn(Inventory inventory, int quantity, Long referenceId) {
        return save(inventory, MovementType.SALES_RETURN, quantity, "Sales return #" + referenceId);
    }

    @Override
    @Transactional
    public InventoryMovement recordDamage(Inventory inventory, int quantity, Long referenceId, String reason) {
        return save(inventory, MovementType.DAMAGE, -quantity,
                "Damage #" + referenceId + (reason != null ? " — " + reason : ""));
    }

    @Override
    @Transactional
    public InventoryMovement recordOpeningStock(Inventory inventory, int quantity) {
        return save(inventory, MovementType.OPENING_STOCK, quantity, "Opening stock");
    }

    private InventoryMovement save(Inventory inventory, MovementType type, int quantity, String note) {
        InventoryMovement movement = InventoryMovement.builder()
                .branch(inventory.getBranch())
                .product(inventory.getProduct())
                .type(type)
                .quantity(quantity)
                .note(note)
                .build();
        return movementRepository.save(movement);
    }
}

/*
 * NOTE — gap found while wiring this against your pasted InventoryMovementDTO:
 * it has no warehouseId, variantId, or referenceId fields, only branchId/
 * productId/type/quantity/note. That means Movement rows currently can't be
 * traced back to WHICH document caused them, or which warehouse/variant was
 * affected if a product has stock in several warehouses.
 *
 * Recommendation: add referenceType/referenceId (mirroring what
 * InventoryTransactionDTO already has) and warehouseId to both the entity
 * and DTO via a small Flyway migration. Until then, this impl folds the
 * reference id into the free-text `note` field as a workaround, which is
 * fine for human reading but not queryable ("find all movements from GRN #42").
 */
