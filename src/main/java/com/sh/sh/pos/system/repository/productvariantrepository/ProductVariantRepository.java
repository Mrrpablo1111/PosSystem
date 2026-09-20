package com.sh.sh.pos.system.repository.productvariantrepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.products.ProductVariant;

public interface ProductVariantRepository
                extends JpaRepository<ProductVariant, Long> {

        Optional<ProductVariant> findBySku(String sku);

        Optional<ProductVariant> findByBarcode(String barcode);

        boolean existsBySku(String sku);

        boolean existsByBarcode(String barcode);

        boolean existsByItemCode(String itemCode);

        List<ProductVariant> findByProductId(Long productId);

        @Query("""
                        SELECT DISTINCT v FROM ProductVariant v
                        LEFT JOIN FETCH v.values
                        WHERE v.product.id = :productId
                        """)
        List<ProductVariant> findByProductIdWithValues(@Param("productId") Long productId);

        @Query("""
                            SELECT pv
                            FROM ProductVariant pv
                            WHERE pv.product.store.id = :storeId
                              AND (
                                LOWER(pv.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                OR LOWER(pv.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                OR LOWER(pv.itemCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                              )
                        """)
        List<ProductVariant> search(@Param("storeId") Long storeId, @Param("keyword") String keyword);

}