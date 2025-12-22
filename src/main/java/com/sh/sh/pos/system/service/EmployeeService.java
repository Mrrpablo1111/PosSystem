package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;

public interface EmployeeService {
	UserDTO createStoreEmployee(UserDTO employee, Long storeId) throws Exception; 
	UserDTO createBranchEmployee(UserDTO employee, Long branchId) throws Exception;
	
	User updateEmployee(Long employeeId, UserDTO employeeDetails) throws Exception;
	
	void deleteEmployee(Long employeeId) throws Exception;
	
	List<UserDTO> findByStoreEmployees(Long storeId, UserRole role) throws Exception;
	List<UserDTO> findByBranchEmployees(Long branchId, UserRole role) throws Exception; 
}
