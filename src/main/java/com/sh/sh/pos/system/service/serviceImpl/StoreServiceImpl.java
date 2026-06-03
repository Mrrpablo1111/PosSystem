package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.StoreMapper;
import com.sh.sh.pos.system.mapper.UserMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.StoreContact;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.StoreService;
import com.sh.sh.pos.system.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;
	private final UserService userService;
	private final BranchRepository branchRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public StoreDTO createStore(StoreDTO storeDTO, User user) {
		Store store = StoreMapper.toEntity(storeDTO, user);

		return StoreMapper.toDTO(storeRepository.save(store));
	}

	@Override
	public StoreDTO getStoreById(Long id) throws ResourceNotFoundException {
		Store store = storeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));
		return StoreMapper.toDTO(store);
	}

	@Override
	public List<StoreDTO> getAllStores(StoreStatus status) {
		List<Store> stores;
		if (status != null) {
			stores = storeRepository.findByStatus(status);
		} else {
			stores = storeRepository.findAll();
		}

		return stores.stream()
				.map(StoreMapper::toDTO)
				.collect(Collectors.toList());

	}

	@Override
	public Store getStoreByAdminId() throws UserException {
		User admin = userService.getCurrentUser();
		return storeRepository.findByStoreAdminId(admin.getId());
	}

	@Override
	public StoreDTO updateStore(Long id, StoreDTO storeDTO) throws UserException {
		User currentUser = userService.getCurrentUser();

		Store existing = storeRepository.findByStoreAdminId(currentUser.getId());

		if (existing == null) {
			throw new UserException("store not found");
		}

		existing.setBrand(storeDTO.getBrand());
		existing.setDescription(storeDTO.getDescription());

		if (storeDTO.getStoreType() != null) {
			existing.setStoreType(storeDTO.getStoreType());
		}
		if (storeDTO.getContact() != null) {
			StoreContact contact = StoreContact.builder()
					.address(storeDTO.getContact().getAddress())
					.phone(storeDTO.getContact().getPhone())
					.email(storeDTO.getContact().getEmail())
					.build();
			existing.setContact(contact);
		}
		Store updateStore = storeRepository.save(existing);

		return StoreMapper.toDTO(updateStore);
	}

	@Override
	public UserDTO addEmployee(Long id, UserDTO userDto) throws UserException {
		Store store = getStoreByAdminId();

		User employee = UserMapper.toEntity(userDto);
		if (userDto.getRole() == UserRole.ROLE_STORE_MANAGER) {
			employee.setStore(store);
		} else if (userDto.getRole() == UserRole.ROLE_BRANCH_MANAGER) {
			Branch branch = branchRepository.findById(userDto.getBranchId()).orElseThrow(
					() -> new EntityNotFoundException("branch not found"));
			employee.setBranch(branch);
			employee.setStore(store);
		}

		employee.setPassword(passwordEncoder.encode(userDto.getPassword()));
		User addedEmployee = userRepository.save(employee);

		return UserMapper.toDTO(addedEmployee);
	}

	@Override
	public void deletedStore() throws ResourceNotFoundException, UserException {
		Store store = getStoreByAdminId();
		if (store == null) {
			throw new ResourceNotFoundException("Store not found");
		}
		storeRepository.deleteById(store.getId());

	}

	@Override
	public StoreDTO getStoreByEmployee() throws UserException {
		User currentUser = userService.getCurrentUser();

		if (currentUser.getStore() == null) {
			throw new UserException("you don't have permission to access this store");
		}
		return StoreMapper.toDTO(currentUser.getStore());
	}

	@Override
	public StoreDTO moderateStore(Long id, StoreStatus status) throws ResourceNotFoundException {
		Store store = storeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("store not found"));
		store.setStatus(status);
		Store updatedStore = storeRepository.save(store);
		return StoreMapper.toDTO(updatedStore);
	}

	@Override
	public List<UserDTO> getEmployeesByStore(Long storeId) throws UserException {
		User currentUser = userService.getCurrentUser();

		Store store = storeRepository.findById(storeId).orElseThrow(
				() -> new EntityNotFoundException("store not found"));
		if (store.getStoreAdmin().getId().equals(currentUser.getId())
				|| currentUser.getStore().getId().equals(store.getId())) {
			List<User> employees = userRepository.findByStoreId(storeId);
			return UserMapper.toDTOList(employees);
		}

		throw new UserException("user does not have enough permissions to access this store");
	}


	

}
