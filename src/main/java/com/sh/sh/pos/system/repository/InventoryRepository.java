package com.sh.sh.pos.system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	
	Inventory findByProductIdAndBranchId(Long branchId, Long productId);
	List<Inventory> findByBranchId(Long branchId);

}
