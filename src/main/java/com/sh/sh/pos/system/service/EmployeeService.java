package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;

public interface EmployeeService {
	UserDTO createStoreEmployee(UserDTO employee, Long storeId) throws Exception; 
	User createBranchEmployee(User employee, Long branchId) throws Exception;
	User updateEmployee(Long employeeId, User employeeDetails) throws Exception;
	void deleteEmployee(Long employeeId) throws Exception;
	User findEmployeeById(Long employeeId) throws Exception;
	List<User> findByStoreEmployees(Long storeId, UserRole role) throws Exception;
	List<User> findByBranchEmployees(Long branchId, UserRole role) throws Exception; 
}
