package com.sh.sh.pos.system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.products.ProductVariantValue;

public interface ProductVariantValueRepository
        extends JpaRepository<ProductVariantValue, Long> {

    List<ProductVariantValue> findByVariantId(Long variantId);
    void deleteByVariantId(Long variantId);

}