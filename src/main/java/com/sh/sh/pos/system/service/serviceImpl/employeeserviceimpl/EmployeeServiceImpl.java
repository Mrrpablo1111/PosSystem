package com.sh.sh.pos.system.service.serviceimpl.employeeserviceimpl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.employeeservice.EmployeeService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private final StoreRepository storeRepository;
	private final BranchRepository branchRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserDTO createStoreEmployee(UserDTO employee, Long storeId) {
		Store store = findStoreOrThrow(storeId);

		Branch branch = null;

		if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER) {
			if (employee.getBranchId() == null) {
				throw new UserException("Branch id is required to create a branch manager");
			}
			branch = findBranchOrThrow(employee.getBranchId());
		}

		User user = UserMapper.toEntity(employee);
		user.setStore(store);
		user.setBranch(branch);
		user.setPassword(passwordEncoder.encode(employee.getPassword()));

		User existing = userRepository.findByEmail(employee.getEmail());
		if (existing != null) {
			user.setId(existing.getId());
		}

		User savedEmployee = userRepository.save(user);
		log.info("Employee saved: id={} email={}", savedEmployee.getId(), savedEmployee.getEmail());

		// Assign manager to branch if applicable
		if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER && branch != null) {
			branch.setManager(savedEmployee);
			branchRepository.save(branch);
		}

		return UserMapper.toDTO(savedEmployee);
	}

	@Override
	@Transactional
	public User createBranchEmployee(User employee, Long branchId) {
		Branch branch = findBranchOrThrow(branchId);

		if (employee.getRole() != UserRole.ROLE_BRANCH_CASHIER
				&& employee.getRole() != UserRole.ROLE_BRANCH_MANAGER) {
			throw new UserException(
					"Invalid role for branch employee. Must be ROLE_BRANCH_CASHIER or ROLE_BRANCH_MANAGER");
		}

		employee.setBranch(branch);
		employee.setPassword(passwordEncoder.encode(employee.getPassword()));

		User existing = userRepository.findByEmail(employee.getEmail());
		if (existing != null) {
			employee.setId(existing.getId());
		}

		return userRepository.save(employee);
	}

	@Override
	@Transactional
	public User updateEmployee(Long employeeId, User employeeDetails) {
		User existingEmployee = findEmployeeById(employeeId);

		if (employeeDetails.getFullName() != null) {
			existingEmployee.setFullName(employeeDetails.getFullName());
		}
		if (employeeDetails.getEmail() != null) {
			existingEmployee.setEmail(employeeDetails.getEmail());
		}
		if (employeeDetails.getPhone() != null) {
			existingEmployee.setPhone(employeeDetails.getPhone());
		}
		if (employeeDetails.getRole() != null) {
			// Add logic to restrict role changes based on current user's role if necessary
			existingEmployee.setRole(employeeDetails.getRole());
		}
		// Password should be updated via a separate method for security reasons

		return userRepository.save(existingEmployee);
	}

	@Override
	@Transactional
	public void deleteEmployee(Long employeeId) {
		User employee = findEmployeeById(employeeId);
		userRepository.delete(employee);
	}

	@Override
	public List<User> findByStoreEmployees(Long storeId, UserRole role) {
		Store store = findStoreOrThrow(storeId);

		return userRepository.findByStoreAndRoleIn(store, List.of(
				UserRole.ROLE_STORE_ADMIN,
				UserRole.ROLE_BRANCH_MANAGER,
				UserRole.ROLE_STORE_MANAGER));
	}

	@Override
	public List<User> findByBranchEmployees(Long branchId, UserRole role) {
		Branch branch = findBranchOrThrow(branchId);

		return userRepository.findByBranchId(branch.getId()).stream()
				.filter(user -> role == null || user.getRole() == role)
				.toList();
	}

	@Override
	public User findEmployeeById(Long employeeId) {
		return userRepository.findById(employeeId).orElseThrow(
				() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
	}

	// ── Helpers ───────────────────────────────────────────────────────────

	private Store findStoreOrThrow(Long storeId) {
		return storeRepository.findById(storeId).orElseThrow(
				() -> new ResourceNotFoundException("Store not found with id: " + storeId));
	}

	private Branch findBranchOrThrow(Long branchId) {
		return branchRepository.findById(branchId).orElseThrow(
				() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
	}
}