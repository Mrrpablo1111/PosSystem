package com.sh.sh.pos.system.mapper.productvariantmapper;



import java.util.List;

import com.sh.sh.pos.system.mapper.productvariantvaluemapper.ProductVariantValueMapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.productdto.ProductVariantDTO;

public class ProductVariantMapper {
    private ProductVariantMapper() {}
    public static ProductVariantDTO toDTO(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
 
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
     public static List<ProductVariantDTO> toDTOList(List<ProductVariant> variants) {
        return variants == null ? List.of()
                : variants.stream().map(ProductVariantMapper::toDTO).toList();
    }

    public static ProductVariant toEntity(ProductVariantDTO dto, Product product) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        applyTo(variant, dto);
        return variant;
    }

      public static void applyTo(ProductVariant variant, ProductVariantDTO dto) {
        variant.setName(dto.getName());
        variant.setSku(dto.getSku());
        variant.setBarcode(dto.getBarcode());
        variant.setItemCode(dto.getItemCode());
 
        variant.setCostPrice(dto.getCostPrice());
        variant.setSellingPrice(dto.getSellingPrice());
 
        // Omitted "active" in the payload must not silently deactivate a variant
        variant.setActive(dto.getActive() != null ? dto.getActive() : Boolean.TRUE);
    }
}
