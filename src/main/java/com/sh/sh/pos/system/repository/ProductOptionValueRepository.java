package com.sh.sh.pos.system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.products.ProductOptionValue;

public interface ProductOptionValueRepository
        extends JpaRepository<ProductOptionValue, Long> {
    boolean existsByOptionIdAndValue(Long optionId, String value);

    List<ProductOptionValue> findByOptionId(Long optionId);

}