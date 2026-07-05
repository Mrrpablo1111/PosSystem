package com.sh.sh.pos.system.repository.branchrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.branch.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long> {
	List<Branch> findByStoreId(Long storeId);

	
}
