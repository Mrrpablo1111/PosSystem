package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.BranchMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.BranchDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.service.BranchService;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor

public class BranchServiceImpl implements BranchService{
	
	private final BranchRepository branchRepository;
	private final StoreRepository storeRepository;
	private final UserService userService;
	
	@Override
	public BranchDTO createBranch(BranchDTO branchDTO) throws UserException {
		User currentUser = userService.getCurrentUser();
		Store store = storeRepository.findByStoreAdminId(currentUser.getId());
		Branch branch = BranchMapper.toEntity(branchDTO, store);
		Branch savedBranch = branchRepository.save(branch);
		
		
		return BranchMapper.toDTO(savedBranch);
	}

	@Override
	public BranchDTO updateBranch(Long id, BranchDTO branchDTO) throws Exception {
		Branch existing = branchRepository.findById(id).orElseThrow(
				()-> new Exception("branch not exists...")
				); 
		
		existing.setName(branchDTO.getName());
		existing.setAddress(branchDTO.getAddress());
		existing.setWorkingDays(branchDTO.getWorkingDays());
		existing.setEmail(branchDTO.getEmail());
		existing.setPhone(branchDTO.getPhone());
		existing.setOpenTime(branchDTO.getOpenTime());
		existing.setCloseTime(branchDTO.getCloseTime());
		existing.setUpdatedAt(LocalDateTime.now());
		
		Branch updatedBranch = branchRepository.save(existing);
		
		return BranchMapper.toDTO(updatedBranch);
	}

	@Override
	public void deleteBranch(Long id) throws Exception {
		Branch existing = branchRepository.findById(id).orElseThrow(
				() -> new Exception("branch not exists...")
				);
		branchRepository.delete(existing); 
	
	}

	@Override
	public List<BranchDTO> getAllBranchesByStoreId(Long storeId) {
		List<Branch> branches = branchRepository.findByStoreId(storeId);
		
		return branches.stream().map(BranchMapper::toDTO)
				.collect(Collectors.toList())
				;
	}

	@Override
	public BranchDTO getBranchById(Long id) throws Exception {
		Branch existing = branchRepository.findById(id).orElseThrow(
				() -> new Exception("branch not exists...")
				);
		return BranchMapper.toDTO(existing);
	}

}
