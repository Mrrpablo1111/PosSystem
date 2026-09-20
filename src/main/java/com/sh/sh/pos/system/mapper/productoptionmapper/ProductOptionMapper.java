package com.sh.sh.pos.system.mapper.productoptionmapper;

import java.util.List;

import com.sh.sh.pos.system.mapper.productoptionvaluemapper.ProductOptionValueMapper;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.payload.dto.productdto.ProductOptionDTO;

public class ProductOptionMapper {

    private ProductOptionMapper() {
    }

    public static ProductOptionDTO toDTO(ProductOption option) {
        if (option == null) {
            return null;
        }
        ProductOptionDTO dto = new ProductOptionDTO();

        dto.setId(option.getId());
        dto.setName(option.getName());
        dto.setProductId(
                option.getProduct() != null
                        ? option.getProduct().getId()
                        : null);
        dto.setValues(option.getValues() == null ? List.of()
                : option.getValues().stream()
                        .map(ProductOptionValueMapper::toDTO)
                        .toList());

        return dto;
    }

    public static ProductOption toEntity(ProductOptionDTO dto, Product product) {
        ProductOption option = new ProductOption();

        option.setName(dto.getName());
        option.setProduct(product);

        return option;
    }
}