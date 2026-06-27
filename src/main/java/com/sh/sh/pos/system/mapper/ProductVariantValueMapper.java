package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.products.ProductOptionValue;
import com.sh.sh.pos.system.model.products.ProductVariantValue;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductVariantValueDTO;

public class ProductVariantValueMapper {
    public static ProductVariantValueDTO toDTO(ProductVariantValue value) {

        ProductVariantValueDTO dto = new ProductVariantValueDTO();

        dto.setId(value.getId());

        dto.setOptionValueId(
                value.getOptionValue().getId());

        dto.setOptionName(
                value.getOptionValue()
                        .getOption()
                        .getName());

        dto.setValue(
                value.getOptionValue()
                        .getValue());

        return dto;
    }

    public static ProductVariantValue toEntity(
            ProductOptionValue optionValue) {

        ProductVariantValue value = new ProductVariantValue();

        value.setOptionValue(optionValue);

        return value;
    }
}
