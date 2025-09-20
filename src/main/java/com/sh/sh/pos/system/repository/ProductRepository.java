package com.sh.sh.pos.system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	
	List<Product> findByStoreId(Long storeId);
	@Query("SELECT p FROM Product p" + 
			"WHERE p.store.id = :storeId AND(" + 
			"LOWER(p.name) LIKE LOWER (CONCAT('%', :query, '%'))" + 
			"Or LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))" +
			"Or LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))" + 
			")")
	List<Product> searchByKeyword(@Param("storeId") Long storeId, @Param("query") String keyword);
}
