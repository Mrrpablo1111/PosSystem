package com.sh.sh.pos.system.service.serviceimpl.branchserviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.branchmapper.BranchMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.branchdto.BranchDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.service.branchservice.BranchService;
import com.sh.sh.pos.system.service.userservice.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository;
	private final StoreRepository storeRepository;
	private final UserService userService;

	@Override
	public BranchDTO createBranch(BranchDTO branchDTO, User user) {
		Store store = storeRepository.findByStoreAdminId(user.getId());
		if (store == null) {
			throw new ResourceNotFoundException("No store found for user id: " + user.getId());
		}

		Branch branch = BranchMapper.toEntity(branchDTO, store);
		return BranchMapper.toDTO(branchRepository.save(branch));
	}

	@Override
	public BranchDTO updateBranch(Long id, BranchDTO branchDTO, User user) {
		Branch existing = findBranchOrThrow(id);

		existing.setName(branchDTO.getName());
		existing.setAddress(branchDTO.getAddress());
		existing.setWorkingDays(branchDTO.getWorkingDays());
		existing.setEmail(branchDTO.getEmail());
		existing.setPhone(branchDTO.getPhone());
		existing.setOpenTime(branchDTO.getOpenTime());
		existing.setCloseTime(branchDTO.getCloseTime());
		if (branchDTO.getBranchType() != null) {
			existing.setBranchType(branchDTO.getBranchType());
		}

		existing.setUpdatedAt(LocalDateTime.now());

		return BranchMapper.toDTO(branchRepository.save(existing));
	}

	@Override
	public void deleteBranch(Long id) {
		branchRepository.delete(findBranchOrThrow(id));
	}

	@Override
	public List<BranchDTO> getAllBranchesByStoreId(Long storeId) {
		User currentUser = userService.getCurrentUser();
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

		// Check if current user is allowed
		boolean isStoreManager = currentUser.getRole() == UserRole.ROLE_STORE_MANAGER &&
				currentUser.getStore() != null &&
				currentUser.getStore().getId().equals(storeId);

		boolean isStoreAdmin = currentUser.getRole() == UserRole.ROLE_STORE_ADMIN &&
				store.getStoreAdmin() != null &&
				store.getStoreAdmin().getId().equals(currentUser.getId());

		if (!isStoreManager && !isStoreAdmin) {
			throw new UserException("You are not authorized to access this store's branches");
		}

		return branchRepository.findByStoreId(store.getId()).stream()
				.map(BranchMapper::toDTO)
				.toList();
	}

	@Override
	public BranchDTO getBranchById(Long id) {
		return BranchMapper.toDTO(findBranchOrThrow(id));
	}

	// ── Helpers ───────────────────────────────────────────────────────────

	private Branch findBranchOrThrow(Long id) {
		return branchRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("Branch not found with id: " + id));
	}
}