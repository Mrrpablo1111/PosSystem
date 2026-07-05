package com.sh.sh.pos.system.service.productservice;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.productdto.ProductVariantValueDTO;

public interface ProductVariantValueService {
    void addValues(Long variantId,
                   List<Long> optionValueIds);

    List<ProductVariantValueDTO> findByVariant(Long variantId);

}
