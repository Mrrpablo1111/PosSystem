package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.BranchDTO;

public interface BranchService {
	
	BranchDTO createBranch(BranchDTO branchDTO) throws UserException;
	BranchDTO updateBranch(Long id, BranchDTO branchDTO, User user) throws Exception;
	void deleteBranch(Long id) throws Exception;
	
	List<BranchDTO> getAllBranchesByStoreId(Long storeId);
	
	BranchDTO getBranchById(Long id) throws Exception;
	
}
