package com.sh.sh.pos.system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {
	private final EmployeeService employeeService;
	
	@PostMapping("/store/{storeId}")
	public ResponseEntity<UserDTO> createStoreEmployee(
			@PathVariable Long storeId, 
			@RequestBody UserDTO userDTO
			) throws Exception{
		UserDTO employee = employeeService.createStoreEmployee(userDTO, storeId);
		return ResponseEntity.ok(employee);
	}
	
	@PostMapping("/branch/{branchId}")
	public ResponseEntity<UserDTO> createBranchEmployee(
			@PathVariable Long branchId,
			@RequestBody UserDTO userDTO
			) throws Exception{
		
		UserDTO employee = employeeService.createBranchEmployee(userDTO, branchId);
		
		return ResponseEntity.ok(employee);
		
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<User> updateEmployee(
			@PathVariable Long id, 
			@RequestBody UserDTO userDTO
			) throws Exception{
		
		User employee = employeeService.updateEmployee(id, userDTO);
		return ResponseEntity.ok(employee);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteEmployee(
			@PathVariable Long id
			) throws Exception{
		employeeService.deleteEmployee(id);
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMessage("Employee deleted");
		return ResponseEntity.ok(apiResponse);
		
	}
	
	@GetMapping("/store/{id}")
	public ResponseEntity<List<UserDTO>> storeEmployee(
			@PathVariable Long id,
			@RequestParam(required = false) UserRole userRole
			) throws Exception{
		
		List<UserDTO> employee = employeeService.findByStoreEmployees(id, userRole);
		
		return ResponseEntity.ok(employee);
		
	}
	
	@GetMapping("/branch/{id}")
	public ResponseEntity<List<UserDTO>> branchEmployee(
			@PathVariable Long id,
			@RequestParam(required = false) UserRole userRole
			) throws Exception{
		
		List<UserDTO> employee = employeeService.findByBranchEmployees(id, userRole);
		
		return ResponseEntity.ok(employee);
		
	}
	
	
	
}
