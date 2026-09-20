package com.sh.sh.pos.system.service.inventoryservice;

import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
// ADAPT: import your real ProductVariant entity package
import com.sh.sh.pos.system.model.products.ProductVariant;

/**
 * Every VALIDATION rule for the inventory domain lives here — nothing else
 * (controller, service, entity) should throw a guard exception directly.
 * Methods read what they need to check but never persist anything.
 */
public interface InventoryValidationService {

    /** @throws com.sh.sh.pos.system.exception.inventory.InventoryNotFoundException if null */
    void validateInventoryExists(Inventory inventory, Long productId, Long variantId, Long warehouseId);

    /** @throws com.sh.sh.pos.system.exception.inventory.WarehouseNotFoundException if null/inactive */
    void validateWarehouseActive(Warehouse warehouse);

    /** @throws com.sh.sh.pos.system.exception.inventory.InventoryValidationException if inactive */
    void validateProductActive(Product product);

    /** @throws com.sh.sh.pos.system.exception.inventory.InventoryValidationException if the
     *  variant does not belong to the given product (cross-product mismatch). */
    void validateVariantBelongsToProduct(ProductVariant variant, Product product);

    /** @throws com.sh.sh.pos.system.exception.inventory.InsufficientStockException if
     *  currentQuantity - requestedDecrease would go below zero. */
    void validateSufficientStock(int currentQuantity, int requestedDecrease);

    /** @throws com.sh.sh.pos.system.exception.inventory.InsufficientStockException if
     *  requestedReserve exceeds currently available quantity. */
    void validateReservationWithinAvailable(int availableQuantity, int requestedReserve);

    /** @throws com.sh.sh.pos.system.exception.inventory.InventoryValidationException if
     *  the inventory row has been soft-deleted (if your Inventory entity supports it —
     *  ADAPT/remove if Inventory has no soft-delete flag). */
    void validateNotDeleted(Inventory inventory);
}
