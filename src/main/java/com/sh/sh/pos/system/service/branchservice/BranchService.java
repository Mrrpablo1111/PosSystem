package com.sh.sh.pos.system.service.branchservice;

import java.util.List;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.branchdto.BranchDTO;

public interface BranchService {
	
	BranchDTO createBranch(BranchDTO branchDTO, User user);
	BranchDTO updateBranch(Long id, BranchDTO branchDTO, User user) throws UserException, ResourceNotFoundException;
	
	List<BranchDTO> getAllBranchesByStoreId(Long storeId);
	void deleteBranch(Long id) throws ResourceNotFoundException;
	BranchDTO getBranchById(Long id);
	
}
