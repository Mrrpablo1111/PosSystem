package com.sh.sh.pos.system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sh.sh.pos.system.model.products.ProductVariant;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(String sku);

    Optional<ProductVariant> findByBarcode(String barcode);

    boolean existsBySku(String sku);


    boolean existsByBarcode(String barcode);

    List<ProductVariant> findByProductId(Long productId);

    @Query("""
                SELECT pv
                FROM ProductVariant pv
                WHERE
                    LOWER(pv.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(pv.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(pv.itemCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<ProductVariant> search(String keyword);

}