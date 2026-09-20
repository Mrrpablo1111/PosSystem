package com.sh.sh.pos.system.mapper.productbarcodemapper;

import java.util.List;

import com.sh.sh.pos.system.domain.BarcodeSource;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBarcode;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.payload.dto.productdto.ProductBarcodeDTO;

public class ProductBarcodeMapper {

    private ProductBarcodeMapper() {
    }

    /** Null/blank → INTERNAL; invalid value → friendly 400 (the UnitType lesson). */
    public static BarcodeSource parseSource(String raw) {
        if (raw == null || raw.isBlank()) {
            return BarcodeSource.INTERNAL;
        }
        try {
            return BarcodeSource.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new com.sh.sh.pos.system.exception.UserException(
                    "Invalid barcode source: '" + raw + "'. Valid values: "
                    + java.util.Arrays.toString(BarcodeSource.values()));
        }
    }

    public static ProductBarcodeDTO toDTO(ProductBarcode entity) {
        if (entity == null) {
            return null;
        }
        ProductBarcodeDTO dto = new ProductBarcodeDTO();
        dto.setId(entity.getId());
        dto.setBarcode(entity.getBarcode());
        dto.setSource(entity.getSource() != null ? entity.getSource().name() : null);
        dto.setNote(entity.getNote());
        dto.setPrimary(entity.getPrimary());
        dto.setActive(entity.getActive());

        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }
        if (entity.getVariant() != null) {
            dto.setVariantId(entity.getVariant().getId());
            dto.setVariantName(entity.getVariant().getName());
        }
        return dto;
    }

    public static List<ProductBarcodeDTO> toDTOList(List<ProductBarcode> entities) {
        return entities == null ? List.of()
                : entities.stream().map(ProductBarcodeMapper::toDTO).toList();
    }

    /** CREATE only — id deliberately not copied. */
    public static ProductBarcode toEntity(ProductBarcodeDTO dto, Product product, ProductVariant variant) {
        return ProductBarcode.builder()
                .product(product)
                .variant(variant)
                .barcode(dto.getBarcode())
                .source(parseSource(dto.getSource()))
                .note(dto.getNote())
                .primary(dto.getPrimary() != null ? dto.getPrimary() : Boolean.FALSE)
                .active(dto.getActive() != null ? dto.getActive() : Boolean.TRUE)
                .build();
    }
}
