package com.sh.sh.pos.system.mapper;

import java.util.stream.Collectors;

import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductVariantDTO;

public class ProductVariantMapper {
    public static ProductVariantDTO toDTO(ProductVariant variant) {

        ProductVariantDTO dto = new ProductVariantDTO();

        dto.setId(variant.getId());

        dto.setProductId(
                variant.getProduct() != null
                        ? variant.getProduct().getId()
                        : null);

        dto.setName(variant.getName());
        dto.setSku(variant.getSku());
        dto.setBarcode(variant.getBarcode());
        dto.setItemCode(variant.getItemCode());

        dto.setCostPrice(variant.getCostPrice());
        dto.setSellingPrice(variant.getSellingPrice());

        dto.setActive(variant.getActive());

        if (variant.getValues() != null) {
            dto.setValues(
                    variant.getValues()
                            .stream()
                            .map(ProductVariantValueMapper::toDTO)
                            .toList());
        }

        return dto;
    }

    public static ProductVariant toEntity(
            ProductVariantDTO dto,
            Product product) {

        ProductVariant variant = new ProductVariant();

        variant.setId(dto.getId());

        variant.setProduct(product);

        variant.setName(dto.getName());
        variant.setSku(dto.getSku());
        variant.setBarcode(dto.getBarcode());
        variant.setItemCode(dto.getItemCode());

        variant.setCostPrice(dto.getCostPrice());
        variant.setSellingPrice(dto.getSellingPrice());

        variant.setActive(dto.getActive());

        return variant;
    }
}
