package com.sh.sh.pos.system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.products.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	
	List<Product> findByStoreId(Long storeId);
	  @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN p.category c
        LEFT JOIN p.supplier s
        WHERE p.store.id = :storeId
        AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
	List<Product> searchByKeyword(@Param("storeId") Long storeId, @Param("query") String keyword);
	boolean existsBySku(String sku);
	boolean existsBySkuAndIdNot(String sku, Long id);
	boolean existsByBarcodeAndIdNot(String barcode, Long id);

	Optional<Product> findBySku(String sku);

	Optional<Product> findByBarcode(String barcode);

	List<Product> findByNameContainingIgnoreCase(String name);

    boolean existsByBarcode(String barcode);



}
