package com.sh.sh.pos.system.service.ProductService;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.productDTO.ProductOptionValueDTO;

public interface ProductOptionValueService {
    ProductOptionValueDTO create(ProductOptionValueDTO dto);

    ProductOptionValueDTO update(Long id, ProductOptionValueDTO dto);

    void delete(Long id);

    List<ProductOptionValueDTO> findByOption(Long optionId);
}
