package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.BranchDTO;

public interface BranchService {
	
	BranchDTO createBranch(BranchDTO branchDTO, User user);
	BranchDTO updateBranch(Long id, BranchDTO branchDTO, User user);
	BranchDTO deleteBranch(Long id);
	
	List<BranchDTO> getAllBranchesByStoreId(Long storeId);
	
	BranchDTO getBranchById(Long id);
	
}
