package com.sh.sh.pos.system.mapper.inventorymapper;

import com.sh.sh.pos.system.mapper.productmapper.ProductMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.payload.dto.inventorydto.InventoryDTO;

public class InventoryMapper {

    private InventoryMapper() {}

    public static InventoryDTO toDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .branchId(inventory.getBranch().getId())
                .productId(inventory.getProduct().getId())
                .product(ProductMapper.toDTO(inventory.getProduct()))
                .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
                .warehouseName(inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : null)
                .variantId(inventory.getProductVariant() != null ? inventory.getProductVariant().getId() : null)
                .variantName(inventory.getProductVariant() != null ? inventory.getProductVariant().getName() : null)
                .quantity(inventory.getQuantity())
                .lowStockAlert(inventory.getLowStockAlert())
                .reorderLevel(inventory.getReorderLevel())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .lowStock(inventory.isLowStock())
                .lastUpdate(inventory.getLastUpdate())
                .build();
    }

    public static Inventory toEntity(InventoryDTO dto, Branch branch, Product product,
                                     Warehouse warehouse, ProductVariant variant) {
        return Inventory.builder()
                .branch(branch)
                .product(product)
                .warehouse(warehouse)
                .productVariant(variant)
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 0)
                .reservedQuantity(dto.getReservedQuantity() != null ? dto.getReservedQuantity() : 0)
                .reorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 0)
                .lowStockAlert(dto.getLowStockAlert())
                .build();
    }
}
