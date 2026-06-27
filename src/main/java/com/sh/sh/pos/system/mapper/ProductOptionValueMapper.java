package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.products.ProductOption;
import com.sh.sh.pos.system.model.products.ProductOptionValue;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductOptionValueDTO;

public class ProductOptionValueMapper {
    public static ProductOptionValueDTO toDTO(ProductOptionValue value) {

        ProductOptionValueDTO dto = new ProductOptionValueDTO();

        dto.setId(value.getId());
        dto.setValue(value.getValue());

        dto.setOptionId(
                value.getOption() != null
                        ? value.getOption().getId()
                        : null);

        return dto;
    }

    public static ProductOptionValue toEntity(
            ProductOptionValueDTO dto,
            ProductOption option) {

        ProductOptionValue value = new ProductOptionValue();

        value.setId(dto.getId());
        value.setValue(dto.getValue());
        value.setOption(option);

        return value;
    }
}
