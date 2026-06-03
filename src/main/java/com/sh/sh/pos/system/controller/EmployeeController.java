package com.sh.sh.pos.system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;
	
	// Create a new employee for a store
	@PostMapping("/store/{storeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
	public ResponseEntity<UserDTO> createStoreEmployee(
			@PathVariable Long storeId, 
			@RequestBody UserDTO employeeDTO
			) throws Exception{
		UserDTO createdEmployee = employeeService.createStoreEmployee(employeeDTO, storeId);
		return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
	}
	
	// Create a new employee for a branch
	@PostMapping("/branch/{branchId}")
	@PreAuthorize("hasAnyRole('ROLE_BRANCH_MANAGER','ROLE_BRANCH_ADMIN')")
	public ResponseEntity<User> createBranchEmployee(
			@PathVariable Long branchId,
			@RequestBody User employee
			) throws Exception{
		
		User createEmployee = employeeService.createBranchEmployee(employee, branchId);
		
		return new ResponseEntity<>(createEmployee, HttpStatus.CREATED);
		
	}
	
	@PutMapping("/{employeeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_ADMIN','ROLE_BRANCH_MANAGER')")
	public ResponseEntity<User> updateEmployee(
			@PathVariable Long employeeId, 
			@RequestBody User employeeDetails
			) throws Exception{
		
		User updateEmployee = employeeService.updateEmployee(employeeId, employeeDetails);
		return new ResponseEntity<>(updateEmployee, HttpStatus.OK);
	}
	
	@DeleteMapping("/{employeeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_BRANCH_ADMIN')")
	public ResponseEntity<Void> deleteEmployee(
			@PathVariable Long employeeId
			) throws Exception{
		employeeService.deleteEmployee(employeeId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
	}
	
	@GetMapping("/{employeeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_ADMIN','ROLE_BRANCH_MANAGER')")
	public ResponseEntity<User> findEmployeeById(@PathVariable Long employeeId) throws Exception{
		User employee = employeeService.findEmployeeById(employeeId);
		return new ResponseEntity<>(employee, HttpStatus.OK);
	}

	@GetMapping("/store/{storeId}")
	@PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER')")
	public ResponseEntity<List<User>> findStoreEmployees(
			@PathVariable Long storeId
			) throws Exception{
		
		List<User> employees = employeeService.findByStoreEmployees(storeId, null);
		
		return new ResponseEntity<>(employees, HttpStatus.OK);
		
	}
	
	@GetMapping("/branch/{branchId}")
	@PreAuthorize("hasAnyRole('ROLE_BRANCH_ADMIN','ROLE_BRANCH_MANAGER')")
	public ResponseEntity<List<User>> findByBranchEmployees(
			@PathVariable Long branchId,
			@RequestParam(required = false) UserRole userRole
			) throws Exception{
		
		List<User> employees = employeeService.findByBranchEmployees(branchId, userRole);
		
		return new ResponseEntity<>(employees, HttpStatus.OK);
		
	}
	
	
	
}
