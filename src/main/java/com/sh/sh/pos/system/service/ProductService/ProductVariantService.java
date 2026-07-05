package com.sh.sh.pos.system.service.productservice;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.productdto.ProductVariantDTO;

public interface ProductVariantService {
    ProductVariantDTO create(ProductVariantDTO dto);

    ProductVariantDTO update(Long id, ProductVariantDTO dto);

    ProductVariantDTO get(Long id);

    List<ProductVariantDTO> getByProduct(Long productId);

    void delete(Long id);
}
