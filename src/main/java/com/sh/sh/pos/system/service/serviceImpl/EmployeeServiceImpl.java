package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.mapper.UserMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.EmployeeService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService{
	
	private final StoreRepository storeRepository;
	private final BranchRepository branchRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	
	@Override
	public UserDTO createStoreEmployee(UserDTO employee, Long storeId) throws Exception {
		Store store= storeRepository.findById(storeId).orElseThrow(
				()-> new Exception("store not found"));
		
		Branch branch = null;
		
		if(employee.getRole()==UserRole.ROLE_BRANCH_MANAGER) {
			if(employee.getBranchId()==null) {
				throw new Exception("branch id is required to create branch manager");
				
			}
			branch = branchRepository.findById(employee.getBranchId()).orElseThrow(
					() -> new Exception("branch not found ")
					);
		}
		
		User user = UserMapper.toEntity(employee);
		user.setStore(store);
		user.setBranch (branch);
		user.setPassword(passwordEncoder.encode(employee.getPassword()));
		
		User savedEmployee = userRepository.save(user);
		if(employee.getRole()==UserRole.ROLE_BRANCH_MANAGER && branch!= null) {
			branch.setManager(savedEmployee);
			branchRepository.save(branch);
			
		}
		
		return UserMapper.toDTO(savedEmployee);
	}

	@Override
	public UserDTO createBranchEmployee(UserDTO employee, Long branchId) throws Exception {
		Branch branch = branchRepository.findById(branchId ).orElseThrow(
				() -> new Exception("branch not found")
				);
		
		//ADMIN
		
		if(employee.getRole()==UserRole.ROLE_BRANCH_CASHIER || 
				employee.getRole()==UserRole.ROLE_BRANCH_MANAGER
				) {
			User user = UserMapper.toEntity(employee);
			user.setBranch(branch);
			user.setPassword(passwordEncoder.encode(employee.getPassword()));
			
			return UserMapper.toDTO(userRepository.save(user));
			
		}
		throw new Exception("branch role not supported"); 
	}

	@Override
	public User updateEmployee(Long employeeId, UserDTO employeeDetails) throws Exception {
		User existingEmployee = userRepository.findById(employeeId).orElseThrow(
				() -> new Exception("employee not exist with given id")
				);
		
		Branch branch= branchRepository.findById(employeeDetails.getBranchId()).orElseThrow(
				() -> new Exception("branch not found")
				);
		
		existingEmployee.setEmail(employeeDetails.getEmail());
		existingEmployee.setFullName(employeeDetails.getFullName());
		existingEmployee.setPassword(employeeDetails.getPassword());
		existingEmployee.setRole(employeeDetails.getRole());
		
		existingEmployee.setBranch(branch);  
		
		return userRepository.save(existingEmployee);
	}

	@Override
	public void deleteEmployee(Long employeeId) throws Exception {
		User employee = userRepository.findById(employeeId).orElseThrow(
				() -> new Exception("Employee not found"));
		
		userRepository.delete(employee);
				
	}

	@Override
	public List<UserDTO> findByStoreEmployees(Long storeId, UserRole role) throws Exception {
		
		Store store = storeRepository.findById(storeId).orElseThrow(
				() -> new Exception("Store not found")
				);
		
		return userRepository.findByStore(store)
				.stream().filter(user -> role == null || user.getRole()==role)
				.map(UserMapper ::toDTO)
				.collect(Collectors.toList());
				
	}

	@Override
	public List<UserDTO> findByBranchEmployees(Long branchId, UserRole role) throws Exception {
		Branch branch = branchRepository.findById(branchId).orElseThrow(
				() -> new Exception("branch not found")
				);
		
		
		
		return userRepository.findByBranchId(branchId)
				.stream().filter(
				user -> role == null || user.getRole()==role
				)
				.map(UserMapper::toDTO)
				.collect(Collectors.toList());
	}

}
