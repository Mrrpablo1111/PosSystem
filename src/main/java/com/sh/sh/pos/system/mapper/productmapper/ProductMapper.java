package com.sh.sh.pos.system.mapper.productmapper;

import java.util.Collections;

import com.sh.sh.pos.system.domain.UnitType;
import com.sh.sh.pos.system.mapper.productvariantmapper.ProductVariantMapper;
import com.sh.sh.pos.system.model.category.Category;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.productdto.ProductDTO;

public class ProductMapper {
    private ProductMapper() {
    }

    public static ProductDTO toDTO(Product product) {
        Category category = product.getCategory();
        Supplier supplier = product.getSupplier();

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .mrp(product.getMrp())
                .costPrice(product.getCostPrice())
                .sellingPrice(product.getSellingPrice())
                .unit(product.getUnit() != null ? product.getUnit().name() : null)
                .barcode(product.getBarcode())
                .brand(product.getBrand())
                .image(product.getImage())
                .active(product.getActive())
                .weight(product.getWeight())
                .supplierId(supplier != null ? supplier.getId() : null)
                .supplierName(supplier != null ? supplier.getName() : null)
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .defaultReorderLevel(product.getDefaultReorderLevel())
                .storeId(product.getStore() != null ? product.getStore().getId() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                // ✅ map variants so the frontend can resolve names without extra requests
                .variants(
                        product.getVariants() != null
                                ? product.getVariants()
                                        .stream()
                                        .map(ProductVariantMapper::toDTO)
                                        .toList()
                                : Collections.emptyList())
                .build();
    }

    public static Product toEntity(ProductDTO productDTO, Store store, Supplier supplier, Category category) {
        return Product.builder()
                .name(productDTO.getName())
                .store(store)
                .category(category)
                .sku(productDTO.getSku())
                .description(productDTO.getDescription())
                .mrp(productDTO.getMrp())
                .costPrice(productDTO.getCostPrice())
                .unit(UnitType.valueOf(productDTO.getUnit()))
                .defaultReorderLevel(productDTO.getDefaultReorderLevel())
                .sellingPrice(productDTO.getSellingPrice())
                .barcode(productDTO.getBarcode())
                .active(productDTO.getActive())
                .weight(productDTO.getWeight())
                .supplier(supplier)
                .brand(productDTO.getBrand())
                .image(productDTO.getImage())
                .createdAt(productDTO.getCreatedAt())
                .updatedAt(productDTO.getUpdatedAt())
                .build();
    }
}