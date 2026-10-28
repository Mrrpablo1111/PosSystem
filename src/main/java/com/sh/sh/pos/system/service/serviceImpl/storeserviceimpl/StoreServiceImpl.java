package com.sh.sh.pos.system.service.serviceimpl.storeserviceimpl;

import java.util.List;
import java.util.Objects;

import com.sh.sh.pos.system.model.store.StoreType;
import com.sh.sh.pos.system.repository.storetyperepository.StoreTypeRepository;
import com.sh.sh.pos.system.security.RolePermissions;
import com.sh.sh.pos.system.security.StoreAuthorization;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.storemapper.StoreMapper;
import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.storedto.StoreDTO;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.storeservice.StoreService;
import com.sh.sh.pos.system.service.userservice.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreTypeRepository storeTypeRepository;
    private final UserService userService;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreAuthorization storeAuthorization;

    @Override
    @Transactional
    public StoreDTO createStore(StoreDTO storeDTO, User storeAdmin) throws UserException {


        if (storeDTO == null) {
            throw new UserException("Store information is required");
        }
        if (storeDTO.getStoreTypeId() == null) {
            throw new UserException("Store type ID is required");
        }
        if (storeAdmin == null || storeAdmin.getId() == null) {
            throw new UserException("Store admin is required");
        }

        User currentUser = getCurrentUser();

        boolean isSuperAdmin = storeAuthorization.isSuperAdmin(currentUser);
        boolean isSameUser = Objects.equals(currentUser.getId(), storeAdmin.getId());
        if (!isSameUser && !isSuperAdmin) {
            throw new UserException(
                    "You cannot create a store for another user"
            );
        }


        StoreType storeType = storeTypeRepository.findById(storeDTO.getStoreTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Store type not found with ID:" + storeDTO.getStoreTypeId()));

        if (!storeType.isActive()) {
            throw new IllegalArgumentException("Cannot create a store with an inactive store type");
        }

        //Enforce the one store per admin rule for everyone
        if (storeRepository.findByStoreAdminId(storeAdmin.getId()).isPresent()) {
            throw new UserException("This user already exists");
        }


        Store store = StoreMapper.toEntity(storeDTO, storeAdmin);

        //StoreMapper.updateStoreType(store, storeType);

        //Assign the relationship once
        store.setStoreType(storeType);
        Store saved = storeRepository.save(store);

        //Maintain the user's store relationship
        storeAdmin.setStore(saved);
        userRepository.save(storeAdmin);

        log.info("✅ Store created successfully:{}, Admin ID: {}", saved.getId(),
                storeAdmin.getId());
        return StoreMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDTO getStoreById(Long id) throws ResourceNotFoundException, UserException {
        Store store = findStoreOrThrow(id);
        User currentUser = getCurrentUser();

        storeAuthorization.requirePermission(currentUser, RolePermissions.STORE_VIEW);
        requireStoreAccess(currentUser, store);
        return StoreMapper.toDTO(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreDTO> getAllStores(StoreStatus status) throws UserException {
        User currentUser = getCurrentUser();

        storeAuthorization.requireSuperAdmin(currentUser);

        List<Store> stores = status == null
                ? storeRepository.findAll()
                : storeRepository.findByStatus(status);

        return stores.stream()
                .map(StoreMapper::toDTO)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public StoreDTO getStoreByAdminId() throws UserException {
        User currentUser = getCurrentUser();
        Store store = storeRepository
                .findByStoreAdminId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No store found for the current admin"
                ));

        return StoreMapper.toDTO(store);
    }

    @Override
    @Transactional
    public StoreDTO updateStore(Long id, StoreDTO storeDTO) throws UserException {

        if (storeDTO == null) {
            throw new UserException("Store information is required");
        }

        User currentUser = getCurrentUser();
        Store store = findStoreOrThrow(id);

        storeAuthorization.requirePermission(
                currentUser,
                RolePermissions.STORE_UPDATE
        );

        storeAuthorization.requireStoreOwnerOrSuperAdmin(
                currentUser,
                store
        );

        // Only update the fields permitted by StoreMapper.
        StoreMapper.updateEntity(store, storeDTO);

        // Change store type only when an ID is provided.
        if (storeDTO.getStoreTypeId() != null) {

            StoreType storeType = storeTypeRepository
                    .findById(storeDTO.getStoreTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Store type not found: "
                                    + storeDTO.getStoreTypeId()
                    ));

            if (!storeType.isActive()) {
                throw new UserException(
                        "Cannot assign an inactive store type"
                );
            }

            store.setStoreType(storeType);
        }

        Store updatedStore = storeRepository.save(store);

        log.info("Store updated successfully. Store ID: {}", id);

        return StoreMapper.toDTO(updatedStore);
    }

    @Override
    public void deactivateStore(Long id) throws UserException, ResourceNotFoundException {

        User currentUser = getCurrentUser();

        Store store = findStoreOrThrow(id);

        storeAuthorization.requirePermission(currentUser, RolePermissions.STORE_DELETE);

        //verify store ownership or super admin access
        storeAuthorization.requireStoreOwnerOrSuperAdmin(currentUser, store);

        if(store.getStatus() == StoreStatus.INACTIVE){
            throw new UserException("Store is already inactive");
        }

        //deactivate instead of physically deleting
        store.setStatus(StoreStatus.INACTIVE);
        storeRepository.save(store);

        log.info("Store deleted successfully. Store ID: {}", store.getId());
    }

    @Override
    @Transactional
    public UserDTO addEmployee(Long id, UserDTO userDTO) throws UserException {
        if (userDTO == null) {
            throw new UserException("Employee information is required");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            throw new UserException("Password is required");
        }
        if (userDTO.getRole() == null) {
            throw new UserException("Role is required");
        }

        User currentUser = getCurrentUser();
        Store store = findStoreOrThrow(id);
        storeAuthorization.requirePermission(currentUser, RolePermissions.STORE_EMPLOYEE_MANAGE);
        storeAuthorization.requireStoreOwnerOrSuperAdmin(currentUser, store);

        User employee = UserMapper.toEntity(userDTO);

        // do not accept a store relationship from client input.
        // the store is selected and verified by the backend

        switch (userDTO.getRole()) {
            case ROLE_STORE_MANAGER -> {
                employee.setStore(store);
            }
            case ROLE_BRANCH_MANAGER -> {
                if (userDTO.getBranchId() == null) {
                    throw new UserException("Branch ID is required for branch manager");
                }

                Branch branch = branchRepository.findById(userDTO.getBranchId()).orElseThrow(
                        () -> new ResourceNotFoundException("Branch ID: " + userDTO.getBranchId())
                );

                if (branch.getStore() == null || !Objects.equals(branch.getStore().getId(), store.getId())) {
                    throw new UserException("The selected branch does not belong to this store");
                }
                employee.setBranch(branch);
                employee.setStore(store);
            }
            default -> throw new UserException("This role cannot be assigned through store employee management");

        }
        employee.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        User savedEmployee = userRepository.save(employee);
        log.info("employee created successfully, Employee ID: {}, Store ID:{}", employee.getId(), store.getId());
        return UserMapper.toDTO(savedEmployee);

    }

    @Override
    public StoreDTO getStoreByEmployee() throws UserException {
        User currentUser = getCurrentUser();

        if (currentUser.getStore() == null) {
            throw new ResourceNotFoundException(
                    "The current user is not assigned to a store"
            );
        }

        Store store = findStoreOrThrow(currentUser.getStore().getId());

        storeAuthorization.requirePermission(currentUser, RolePermissions.STORE_VIEW);

        return StoreMapper.toDTO(store);
    }

    // Super admin only
    @Override
    @Transactional
    public StoreDTO moderateStore(Long id, StoreStatus status) throws UserException {
        if (status == null) {
            throw new UserException("Store status is required");
        }

        User currentUser = getCurrentUser();
        storeAuthorization.requireSuperAdmin(currentUser);
        Store store = findStoreOrThrow(id);
        store.setStatus(status);
        Store updated = storeRepository.save(store);
        log.info(
                "Store status updated: id={}, status={}",
                updated.getId(),
                updated.getStatus()
        );
        return StoreMapper.toDTO(updated);
    }

    @Override
    public List<UserDTO> getEmployeesByStore(Long storeId) throws UserException {
        User currentUser = getCurrentUser();
        Store store = findStoreOrThrow(storeId);

        storeAuthorization.requirePermission(
                currentUser,
                RolePermissions.STORE_EMPLOYEE_MANAGE
        );
        requireStoreAccess(currentUser, store);

        return userRepository.findByStoreId(storeId)
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    // Private Helper
    private User getCurrentUser() throws UserException {

        User currentUser = userService.getCurrentUser();

        if (currentUser == null || currentUser.getId() == null) {
            throw new UserException("Authentication is required");
        }

        return currentUser;
    }

    private Store findStoreOrThrow(Long id) throws UserException {

        if (id == null) {
            throw new UserException("Store ID is required");
        }

        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found with ID: " + id
                ));
    }

    private void requireStoreAccess(User user, Store store) throws UserException {

        if (storeAuthorization.isSuperAdmin(user)) {
            return;
        }

        boolean isOwner = store.getStoreAdmin() != null
                && Objects.equals(
                store.getStoreAdmin().getId(),
                user.getId()
        );

        boolean isStoreMember = user.getStore() != null
                && Objects.equals(
                user.getStore().getId(),
                store.getId()
        );

        if (!isOwner && !isStoreMember) {
            throw new UserException(
                    "You do not have access to this store"
            );
        }
    }
}
