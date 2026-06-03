package com.sh.sh.pos.system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	
	Inventory findByProductId(Long productId);
	List<Inventory> findByBranchId(Long branchId);
	@Query("""
        SELECT COUNT(i)
        FROM Inventory i
        JOIN i.product p
        WHERE i.branch.id = :branchId
        AND i.quantity <= 5
    """)
    int countLowStockItems(@Param("branchId") Long branchId);
}
