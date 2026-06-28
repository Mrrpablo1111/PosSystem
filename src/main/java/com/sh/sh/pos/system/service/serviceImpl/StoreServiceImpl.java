package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;
	private final UserService userService;
	private final BranchRepository branchRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public StoreDTO createStore(StoreDTO storeDTO, User user) {
		Store store = StoreMapper.toEntity(storeDTO, user);
        Store saved = storeRepository.save(store);
        log.info("✅ Store created: id={} brand={}", saved.getId(), saved.getBrand());
        return StoreMapper.toDTO(saved);
	}

	@Override
	public StoreDTO getStoreById(Long id) throws ResourceNotFoundException {
		  Store store = findStoreOrThrow(id);
        return StoreMapper.toDTO(store);
	}

	@Override
	public List<StoreDTO> getAllStores(StoreStatus status) {
		List<Store> stores = status != null
                ? storeRepository.findByStatus(status)
                : storeRepository.findAll();
 
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
    @Transactional
    public StoreDTO updateStore(Long id, StoreDTO dto) throws UserException {
        User currentUser = userService.getCurrentUser();
        Store store      = storeRepository.findByStoreAdminId(currentUser.getId());
 
        if (store == null) {
            throw new UserException("Store not found for current user");
        }
 
        StoreMapper.updateEntity(store, dto);
        Store updated = storeRepository.save(store);
        log.info("✅ Store updated: id={}", updated.getId());
        return StoreMapper.toDTO(updated);
    }
 

	 @Override
    @Transactional
    public UserDTO addEmployee(Long id, UserDTO userDto) throws UserException {
        Store store    = getStoreByAdminId();
        User  employee = UserMapper.toEntity(userDto);
 
        switch (userDto.getRole()) {
            case ROLE_STORE_MANAGER -> employee.setStore(store);
            case ROLE_BRANCH_MANAGER -> {
                Branch branch = branchRepository.findById(userDto.getBranchId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Branch not found: " + userDto.getBranchId()));
                employee.setBranch(branch);
                employee.setStore(store);
            }
            default -> throw new UserException("Invalid employee role: " + userDto.getRole());
        }
 
        employee.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User saved = userRepository.save(employee);
        log.info("✅ Employee added: id={} role={}", saved.getId(), saved.getRole());
        return UserMapper.toDTO(saved);
	}

	@Override
    @Transactional
    public void deletedStore() throws ResourceNotFoundException, UserException {
        Store store = getStoreByAdminId();
        if (store == null) {
            throw new ResourceNotFoundException("Store not found");
        }
        storeRepository.deleteById(store.getId());
        log.info("✅ Store deleted: id={}", store.getId());
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
    @Transactional
    public StoreDTO moderateStore(Long id, StoreStatus status)
            throws ResourceNotFoundException {
        Store store = findStoreOrThrow(id);
        store.setStatus(status);
        Store updated = storeRepository.save(store);
        log.info("✅ Store status updated: id={} status={}", id, status);
        return StoreMapper.toDTO(updated);
    }

	@Override
    public List<UserDTO> getEmployeesByStore(Long storeId) throws UserException {
        User  currentUser = userService.getCurrentUser();
        Store store       = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Store not found: " + storeId));
 
        boolean isAdmin   = store.getStoreAdmin().getId().equals(currentUser.getId());
        boolean isMember  = currentUser.getStore() != null &&
                            currentUser.getStore().getId().equals(store.getId());
 
        if (!isAdmin && !isMember) {
            throw new UserException("You do not have permission to access this store");
        }
 
        return userRepository.findByStoreId(storeId)
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

	private Store findStoreOrThrow(Long id) throws ResourceNotFoundException {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found: " + id));
    }

	

}
