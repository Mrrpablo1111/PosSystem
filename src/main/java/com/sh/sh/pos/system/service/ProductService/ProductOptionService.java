package com.sh.sh.pos.system.service.productservice;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.productdto.ProductOptionDTO;

public interface ProductOptionService {

    ProductOptionDTO create(ProductOptionDTO productOptionDTO);

    ProductOptionDTO update(Long id, ProductOptionDTO productOptionDTO);

    void delete(Long id);

    List<ProductOptionDTO> findByProduct(Long productId);
    
}
