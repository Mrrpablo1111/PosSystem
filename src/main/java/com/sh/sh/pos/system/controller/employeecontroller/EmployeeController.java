package com.sh.sh.pos.system.controller.employeecontroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.service.employeeservice.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	// Create a new employee for a store
	@PostMapping("/store/{storeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
	public ResponseEntity<UserDTO> createStoreEmployee(
			@PathVariable Long storeId,
			@RequestBody UserDTO employeeDTO) {
		UserDTO createdEmployee = employeeService.createStoreEmployee(employeeDTO, storeId);
		return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
	}

	// Create a new employee for a branch
	@PostMapping("/branch/{branchId}")
	@PreAuthorize("hasAnyRole('ROLE_BRANCH_MANAGER','ROLE_BRANCH_ADMIN')")
	public ResponseEntity<User> createBranchEmployee(
			@PathVariable Long branchId,
			@RequestBody User employee) {
		User createdEmployee = employeeService.createBranchEmployee(employee, branchId);
		return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
	}

	@PutMapping("/{employeeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_ADMIN','ROLE_BRANCH_MANAGER')")
	public ResponseEntity<User> updateEmployee(
			@PathVariable Long employeeId,
			@RequestBody User employeeDetails) {
		User updatedEmployee = employeeService.updateEmployee(employeeId, employeeDetails);
		return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
	}

	@DeleteMapping("/{employeeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_BRANCH_ADMIN')")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long employeeId) {
		employeeService.deleteEmployee(employeeId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/{employeeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_ADMIN','ROLE_BRANCH_MANAGER')")
	public ResponseEntity<User> findEmployeeById(@PathVariable Long employeeId) {
		User employee = employeeService.findEmployeeById(employeeId);
		return new ResponseEntity<>(employee, HttpStatus.OK);
	}

	@GetMapping("/store/{storeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER')")
	public ResponseEntity<List<User>> findStoreEmployees(@PathVariable Long storeId) {
		List<User> employees = employeeService.findByStoreEmployees(storeId, null);
		return new ResponseEntity<>(employees, HttpStatus.OK);
	}

	@GetMapping("/branch/{branchId}")
	@PreAuthorize("hasAnyRole('ROLE_BRANCH_ADMIN','ROLE_BRANCH_MANAGER')")
	public ResponseEntity<List<User>> findByBranchEmployees(
			@PathVariable Long branchId,
			@RequestParam(required = false) UserRole userRole) {
		List<User> employees = employeeService.findByBranchEmployees(branchId, userRole);
		return new ResponseEntity<>(employees, HttpStatus.OK);
	}
}