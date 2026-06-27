package com.sh.sh.pos.system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.products.ProductOption;

public interface ProductOptionRepository
        extends JpaRepository<ProductOption, Long> {

    List<ProductOption> findByProductId(Long productId);

}