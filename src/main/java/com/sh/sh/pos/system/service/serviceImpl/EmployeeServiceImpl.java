package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.UserMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.EmployeeService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private final StoreRepository storeRepository;
	private final BranchRepository branchRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserDTO createStoreEmployee(UserDTO employee, Long storeId) throws Exception {
		Store store = storeRepository.findById(storeId).orElseThrow(
				() -> new Exception("store not found" + storeId));

		Branch branch = null;

		if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER) {
			if (employee.getBranchId() == null) {
				throw new Exception("branch id is required to create branch manager");

			}
			branch = branchRepository.findById(employee.getBranchId()).orElseThrow(
					() -> new Exception("branch not found "));
		}

		User user = UserMapper.toEntity(employee);
		user.setStore(store);
		user.setBranch(branch);
		user.setPassword(passwordEncoder.encode(employee.getPassword()));

		User isExist = userRepository.findByEmail(employee.getEmail());

		System.out.print("isExist" + isExist);
		if (isExist != null) {
			employee.setId(isExist.getId());
		}

		User savedEmployee = userRepository.save(user);

		System.out.println("savedEmployee:" + savedEmployee);

		// Assign manager to branch if applicable
		if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER && branch != null) {
			branch.setManager(savedEmployee);
			branchRepository.save(branch);

		}

		return UserMapper.toDTO(savedEmployee);
	}

	@Override
	public User createBranchEmployee(User employee, Long branchId) throws Exception {
		Branch branch = branchRepository.findById(branchId).orElseThrow(
				() -> new ResourceNotFoundException("branch not found with ID"+branchId));

		if (!(employee.getRole().equals(UserRole.ROLE_BRANCH_CASHIER) ||
				employee.getRole().equals(UserRole.ROLE_BRANCH_MANAGER))) {

			throw new UserException("Invalid role for branch employee. Must be ROLE_BRANCH_ADMIN or ROLE_BRANCH_MANAGER");
			
		}
			employee.setBranch(branch);
			employee.setPassword(passwordEncoder.encode(employee.getPassword()));
			User isExist = userRepository.findByEmail(employee.getEmail());
			if(isExist!=null){
				employee.setId(isExist.getId());
			}
			return userRepository.save(employee);

	}

	@Override
	public User updateEmployee(Long employeeId, User employeeDetails) throws Exception {
		User existingEmployee = userRepository.findById(employeeId).orElseThrow(
				() -> new Exception("employee not exist with given id"));

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
	public void deleteEmployee(Long employeeId) throws Exception {
		User employee = userRepository.findById(employeeId).orElseThrow(
				() -> new Exception("Employee not found"));

		userRepository.delete(employee);

	}

	@Override
	public List<User> findByStoreEmployees(Long storeId, UserRole role) throws Exception {

		Store store = storeRepository.findById(storeId).orElseThrow(
				() -> new ResourceNotFoundException("Store not found with ID" + storeId));

		return userRepository.findByStoreAndRoleIn(store, List.of(
			UserRole.ROLE_STORE_ADMIN,
			UserRole.ROLE_BRANCH_MANAGER,
			UserRole.ROLE_STORE_MANAGER
		));
			

	}

	@Override
	public List<User> findByBranchEmployees(Long branchId, UserRole role) throws Exception {
		Branch branch = branchRepository.findById(branchId).orElseThrow(
				() -> new ResourceNotFoundException("branch not found"));

		List<User> employees = userRepository.findByBranchId(branch.getId()).stream()
							.filter(user-> role == null||user.getRole()==role)
							.collect(Collectors.toList());
							return employees;
	}

	@Override
	public User findEmployeeById(Long employeeId) throws Exception {
		Optional<User> opt = userRepository.findById(employeeId);
		if(opt.isPresent()){
			return opt.get();

		}
		throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
	}

}
