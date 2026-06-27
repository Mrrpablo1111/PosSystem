package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.BranchMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.BranchDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.BranchService;
import com.sh.sh.pos.system.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository;
	private final StoreRepository storeRepository;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	@Override
	public BranchDTO createBranch(BranchDTO branchDTO, User user) {

		Store store = storeRepository.findByStoreAdminId(user.getId());
		Branch branch = BranchMapper.toEntity(branchDTO, store);

		return BranchMapper.toDTO(branchRepository.save(branch));
	}

	@Override
	public BranchDTO updateBranch(Long id, BranchDTO branchDTO, User user) throws Exception {
		Branch existing = branchRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("branch not exists..."));

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
	public void deleteBranch(Long id) throws Exception {
		Branch existing = branchRepository.findById(id).orElseThrow(
				() -> new Exception("branch not exists..."));
		branchRepository.delete(existing);

	}

	@Override
	public List<BranchDTO> getAllBranchesByStoreId(Long storeId) throws UserException {
		User currentUser = userService.getCurrentUser();
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new EntityNotFoundException("Store not found"));

		// Check if Current user is allowed
		boolean isStoreManager = currentUser.getRole() == UserRole.ROLE_STORE_MANAGER &&
				currentUser.getStore() != null &&
				currentUser.getStore().getId().equals(storeId);

		boolean isStoreAdmin = currentUser.getRole() == UserRole.ROLE_STORE_ADMIN &&
				store.getStoreAdmin() != null &&
				store.getStoreAdmin().getId().equals(currentUser.getId());

		if (!isStoreManager && !isStoreAdmin) {
			throw new UserException("You are not authorized to access this store's branches");
		}

		return branchRepository.findByStoreId(store.getId()).stream().map(BranchMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public BranchDTO getBranchById(Long id) {
		Branch branch = branchRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("branch not exists..."));
		return BranchMapper.toDTO(branch);
	}

}