package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.BranchDTO;
import com.sh.sh.pos.system.service.BranchService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor

public class BranchServiceImpl implements BranchService{

	@Override
	public BranchDTO createBranch(BranchDTO branchDTO, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BranchDTO updateBranch(Long id, BranchDTO branchDTO, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BranchDTO deleteBranch(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BranchDTO> getAllBranchesByStoreId(Long storeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BranchDTO getBranchById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
