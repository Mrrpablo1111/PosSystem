package com.sh.sh.pos.system.service.serviceimpl.inventoryserviceimpl;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exception.inventoryexception.InsufficientStockException;
import com.sh.sh.pos.system.exception.inventoryexception.InventoryNotFoundException;
import com.sh.sh.pos.system.exception.inventoryexception.InventoryValidationException;
import com.sh.sh.pos.system.exception.inventoryexception.WarehouseNotFoundException;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.service.inventoryservice.InventoryValidationService;

@Service
public class InventoryValidationServiceImpl implements InventoryValidationService {

    @Override
    public void validateInventoryExists(Inventory inventory, Long productId, Long variantId, Long warehouseId) {
        if (inventory == null) {
            throw new InventoryNotFoundException(
                    "No inventory found for product " + productId
                    + (variantId != null ? " variant " + variantId : "")
                    + " in warehouse " + warehouseId);
        }
    }

    @Override
    public void validateWarehouseActive(Warehouse warehouse) {
        if (warehouse == null) {
            throw new WarehouseNotFoundException("Warehouse not found");
        }
        // ADAPT: uncomment if Warehouse has an `active` flag
        // if (Boolean.FALSE.equals(warehouse.getActive())) {
        //     throw new WarehouseNotFoundException("Warehouse '" + warehouse.getName() + "' is inactive");
        // }
    }

    @Override
    public void validateProductActive(Product product) {
        if (product == null) {
            throw new InventoryValidationException("Product not found");
        }
        if (Boolean.FALSE.equals(product.getActive())) {
            throw new InventoryValidationException(
                    "Cannot receive inactive product: " + product.getName());
        }
    }

    @Override
    public void validateVariantBelongsToProduct(ProductVariant variant, Product product) {
        if (variant == null) return;   // no variant scope requested — nothing to validate
        if (variant.getProduct() == null || !variant.getProduct().getId().equals(product.getId())) {
            throw new InventoryValidationException(
                    "Variant " + variant.getId() + " does not belong to product " + product.getId());
        }
    }

    @Override
    public void validateSufficientStock(int currentQuantity, int requestedDecrease) {
        if (requestedDecrease < 0) {
            throw new InventoryValidationException("Decrease quantity cannot be negative");
        }
        if (currentQuantity - requestedDecrease < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock: have " + currentQuantity + ", requested decrease " + requestedDecrease);
        }
    }

    @Override
    public void validateReservationWithinAvailable(int availableQuantity, int requestedReserve) {
        if (requestedReserve < 0) {
            throw new InventoryValidationException("Reservation quantity cannot be negative");
        }
        if (requestedReserve > availableQuantity) {
            throw new InsufficientStockException(
                    "Cannot reserve " + requestedReserve + " — only " + availableQuantity + " available");
        }
    }

    @Override
    public void validateNotDeleted(Inventory inventory) {
        // ADAPT: only relevant if Inventory has a soft-delete flag; remove otherwise
        // if (Boolean.TRUE.equals(inventory.getDeleted())) {
        //     throw new InventoryValidationException("Inventory " + inventory.getId() + " has been deleted");
        // }
    }
}
