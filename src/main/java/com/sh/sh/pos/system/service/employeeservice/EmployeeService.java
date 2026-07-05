package com.sh.sh.pos.system.service.employeeservice;

import java.util.List;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;

public interface EmployeeService {
	UserDTO createStoreEmployee(UserDTO employee, Long storeId); 
	User createBranchEmployee(User employee, Long branchId);
	User updateEmployee(Long employeeId, User employeeDetails);
	void deleteEmployee(Long employeeId);
	User findEmployeeById(Long employeeId);
	List<User> findByStoreEmployees(Long storeId, UserRole role);
	List<User> findByBranchEmployees(Long branchId, UserRole role);
}
