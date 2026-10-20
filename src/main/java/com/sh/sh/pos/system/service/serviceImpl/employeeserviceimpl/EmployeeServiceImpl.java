package com.sh.sh.pos.system.service.serviceimpl.employeeserviceimpl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	// Create Store Employee
	@Override
	@Transactional
	public UserDTO createStoreEmployee(UserDTO employee, Long storeId) {

		if (employee == null) {
			throw new UserException("Employee information is required");
		}

		if (storeId == null) {
			throw new UserException("Store id is required");
		}

		Store store = findStoreOrThrow(storeId);

		validateStoreEmployeeRole(employee.getRole());

		validateEmail(employee.getEmail());

		checkEmailNotExists(employee.getEmail());

		Branch branch = null;

		if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER) {
			if (employee.getBranchId() == null) {
				throw new UserException("Branch id is required to create a branch manager");
			}
			branch = findBranchOrThrow(employee.getBranchId());

			// Make sure branch belongs to this store
			validateBranchBelongsToStore(branch, store);
		}

		User user = UserMapper.toEntity(employee);
		user.setStore(store);
		user.setBranch(branch);

		user.setPassword(passwordEncoder.encode(employee.getPassword()));

		user.setVerified(false);

		User savedEmployee = userRepository.save(user);
		log.info(
				"Store employee created: id={}, email={}, role={}, storeId={}",
				savedEmployee.getId(),
				savedEmployee.getEmail(),
				savedEmployee.getRole(),
				storeId);

		// Assign manager to branch if applicable
		if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER) {

			if (branch == null) {
				throw new UserException(
						"Branch is required for branch manager");
			}

			branch.setManager(savedEmployee);

			branchRepository.save(branch);

			log.info(
					"Branch manager assigned: userId={}, branchId={}",
					savedEmployee.getId(),
					branch.getId());
		}

		return UserMapper.toDTO(savedEmployee);
	}

	// Create Branch Employee
	@Override
	@Transactional
	public User createBranchEmployee(User employee, Long branchId) {
		if (employee == null) {
			throw new UserException("Employee information is required");
		}

		if (branchId == null) {
			throw new UserException("Branch id is required");
		}
		Branch branch = findBranchOrThrow(branchId);

		validateBranchEmployeeRole(employee.getRole());

		validateEmail(employee.getEmail());

		checkEmailNotExists(employee.getEmail());

		if (branch.getStore() == null) {
			throw new UserException(
					"Branch is not associated with a store");
		}

		if (employee.getRole() != UserRole.ROLE_BRANCH_CASHIER
				&& employee.getRole() != UserRole.ROLE_BRANCH_MANAGER) {
			throw new UserException(
					"Invalid role for branch employee. Must be ROLE_BRANCH_CASHIER or ROLE_BRANCH_MANAGER");
		}

		employee.setStore(branch.getStore());

		employee.setBranch(branch);
		employee.setPassword(passwordEncoder.encode(employee.getPassword()));

		employee.setId(null);
		employee.setVerified(false);

		User savedEmployee = userRepository.save(employee);

		log.info(
				"Branch employee created: id={}, email={}, role={}, branchId={}, storeId={}",
				savedEmployee.getId(),
				savedEmployee.getEmail(),
				savedEmployee.getRole(),
				branch.getId(),
				branch.getStore().getId());

		if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER) {

			branch.setManager(savedEmployee);

			branchRepository.save(branch);

			log.info(
					"Branch manager assigned: userId={}, branchId={}",
					savedEmployee.getId(),
					branch.getId());
		}

		return savedEmployee;
	}

	// Update Employee
	@Override
	@Transactional
	public User updateEmployee(Long employeeId, User employeeDetails) {
		User existingEmployee = findEmployeeById(employeeId);

		if (employeeDetails == null) {
			throw new UserException(
					"Employee information is required");
		}

		if (employeeDetails.getFullName() != null
				&& !employeeDetails.getFullName().isBlank()) {

			existingEmployee.setFullName(
					employeeDetails.getFullName().trim());
		}
		if (employeeDetails.getEmail() != null
				&& !employeeDetails.getEmail().isBlank()) {

			String newEmail = employeeDetails.getEmail().trim();

			if (!newEmail.equalsIgnoreCase(
					existingEmployee.getEmail())) {

				checkEmailNotExists(newEmail);

				existingEmployee.setEmail(newEmail);
			}
		}
		if (employeeDetails.getPhone() != null) {

			existingEmployee.setPhone(
					employeeDetails.getPhone().trim());
		}
		if (employeeDetails.getRole() != null) {

			validateEmployeeRoleChange(
					existingEmployee.getRole(),
					employeeDetails.getRole());

			existingEmployee.setRole(
					employeeDetails.getRole());
		}
		User savedEmployee = userRepository.save(existingEmployee);

		log.info(
				"Employee updated: id={}, email={}",
				savedEmployee.getId(),
				savedEmployee.getEmail());

		return savedEmployee;
	}

	// Delete Employee
	@Override
	@Transactional
	public void deleteEmployee(Long employeeId) {
		User employee = findEmployeeById(employeeId);
		if (employee.getRole() == UserRole.ROLE_ADMIN) {
			throw new UserException(
					"Super admin cannot be deleted through employee service");
		}

		if (employee.getRole() == UserRole.ROLE_STORE_ADMIN) {
			throw new UserException(
					"Store admin cannot be deleted through employee service");
		}

		if (employee.getBranch() != null
				&& employee.getBranch().getManager() != null
				&& employee.getBranch().getManager().getId()
						.equals(employee.getId())) {

			Branch branch = employee.getBranch();

			branch.setManager(null);

			branchRepository.save(branch);
		}
		userRepository.delete(employee);

		log.info(
				"Employee deleted: id={}, email={}",
				employee.getId(),
				employee.getEmail());
	}

	// Find Store Employee
	@Override
	@Transactional(readOnly = true)
	public List<User> findByStoreEmployees(Long storeId, UserRole role) {
		Store store = findStoreOrThrow(storeId);

		List<UserRole> allowedRoles = List.of(
				UserRole.ROLE_STORE_MANAGER,
				UserRole.ROLE_BRANCH_MANAGER,
				UserRole.ROLE_BRANCH_CASHIER);

		if (role != null) {

			if (!allowedRoles.contains(role)) {
				throw new UserException(
						"Invalid employee role for store");
			}

			return userRepository
					.findByStoreAndRoleIn(
							store,
							List.of(role));
		}

		return userRepository
				.findByStoreAndRoleIn(
						store,
						allowedRoles);

	}

	// Find Branch By Employee
	@Override
	@Transactional(readOnly = true)
	public List<User> findByBranchEmployees(Long branchId, UserRole role) {
		Branch branch = findBranchOrThrow(branchId);

		if (role != null) {

			validateBranchEmployeeRole(role);

			return userRepository
					.findByBranchAndRoleIn(
							branch,
							List.of(role));
		}

		return userRepository
				.findByBranchAndRoleIn(
						branch,
						List.of(
								UserRole.ROLE_BRANCH_CASHIER,
								UserRole.ROLE_BRANCH_MANAGER));
	}

	// Find Employee By Id
	@Override
	@Transactional(readOnly = true)
	public User findEmployeeById(Long employeeId) {
		if (employeeId == null) {
			throw new UserException(
					"Employee id is required");
		}

		return userRepository
				.findById(employeeId)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								"Employee not found with id: "
										+ employeeId));
	}

	// Validation Store Employee Role

	private void validateStoreEmployeeRole(
            UserRole role) {

        if (role == null) {
            throw new UserException(
                    "Employee role is required"
            );
        }

        if (role != UserRole.ROLE_STORE_MANAGER
                && role != UserRole.ROLE_BRANCH_MANAGER
                && role != UserRole.ROLE_BRANCH_CASHIER) {

            throw new UserException(
                    "Invalid store employee role"
            );
        }
    }

	// Validation Branch Employee Role

	 private void validateBranchEmployeeRole(
            UserRole role) {

        if (role == null) {
            throw new UserException(
                    "Employee role is required"
            );
        }

        if (role != UserRole.ROLE_BRANCH_CASHIER
                && role != UserRole.ROLE_BRANCH_MANAGER) {

            throw new UserException(
                    "Invalid branch employee role. "
                    + "Allowed roles: ROLE_BRANCH_CASHIER, "
                    + "ROLE_BRANCH_MANAGER"
            );
        }
    }

	// Validate Email

	private void validateEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new UserException(
                    "Email is required"
            );
        }
    }

	//Check Duplicate Email

	private void checkEmailNotExists(String email){
		if(userRepository.existsByEmail(email)){
			throw new UserException(
                    "User with email already exists: "
                            + email
            );
		}
	}

	//  Validate Branch Belongs to Store

	private void validateBranchBelongsToStore(
            Branch branch,
            Store store) {

        if (branch.getStore() == null) {
            throw new UserException(
                    "Branch is not associated with a store"
            );
        }

        if (!branch.getStore()
                .getId()
                .equals(store.getId())) {

            throw new UserException(
                    "Branch does not belong to this store"
            );
        }
    }

	// Validate Role Change
	private void validateEmployeeRoleChange(
            UserRole currentRole,
            UserRole newRole) {

        if (newRole == UserRole.ROLE_ADMIN) {
            throw new UserException(
                    "Employee cannot be changed to SUPER_ADMIN"
            );
        }

        if (newRole == UserRole.ROLE_STORE_ADMIN) {
            throw new UserException(
                    "Employee cannot be changed to STORE_ADMIN"
            );
        }

        if (currentRole == UserRole.ROLE_BRANCH_MANAGER
                && newRole != UserRole.ROLE_BRANCH_MANAGER) {

            // Manager assignment should be handled separately.
            // We don't silently change the role here.
            throw new UserException(
                    "Branch manager role must be changed "
                    + "through branch manager management"
            );
        }
    }
	// ── Helpers ───────────────────────────────────────────────────────────

	private Store findStoreOrThrow(Long storeId) {

        if (storeId == null) {
            throw new UserException(
                    "Store id is required"
            );
        }

        return storeRepository
                .findById(storeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Store not found with id: "
                                        + storeId
                        )
                );
    }

	private Branch findBranchOrThrow(Long branchId) {

        if (branchId == null) {
            throw new UserException(
                    "Branch id is required"
            );
        }

        return branchRepository
                .findById(branchId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Branch not found with id: "
                                        + branchId
                        )
                );
    }
}