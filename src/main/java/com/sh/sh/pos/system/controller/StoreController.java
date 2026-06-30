package com.sh.sh.pos.system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.StoreMapper;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.StoreService;
import com.sh.sh.pos.system.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Store", description = "Store management endpoints")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {
	private final StoreService storeService;
	private final UserService userService;

	// Create new Store
	@PostMapping("/create")
	public ResponseEntity<StoreDTO> createStore(@Valid @RequestBody StoreDTO storeDTO,
			@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.getUserFromJwtToken(jwt);

		return ResponseEntity.ok(storeService.createStore(storeDTO, user));
	}

	// Get Store By ID
	@GetMapping("/{id}")
	public ResponseEntity<StoreDTO> getStoreById(@PathVariable Long id)
			throws ResourceNotFoundException {
		return ResponseEntity.ok(storeService.getStoreById(id));
	}

	//Get Store By admin user ID
	@GetMapping("/admin")
	public ResponseEntity<StoreDTO> getStoreByAdmin() throws UserException {
		Store store = storeService.getStoreByAdminId();
		return ResponseEntity.ok(StoreMapper.toDTO(store));
	}

	// Get all stores(admin)
	@GetMapping()
	public ResponseEntity<List<StoreDTO>> getAllStores(@RequestParam(required = false) StoreStatus status) {
		return ResponseEntity.ok(storeService.getAllStores(status));
	}

	/**
	 * Approve or decline a store request
	 * 
	 * @param storeId the store ID
	 * @param action  the action to perform (APPROVE or DECLINE)
	 * @return updated StoreDTO
	 * @throws ResourceNotFoundException
	 */
	@PutMapping("/{storeId}/moderate")
	public StoreDTO moderateStore(@PathVariable Long storeId, @RequestParam StoreStatus status
			) throws ResourceNotFoundException {
		return storeService.moderateStore(storeId, status);
	}

	// Get Store By employee user ID
	@GetMapping("/employee")
	public ResponseEntity<StoreDTO> getStoreByEmployee()
			throws UserException {
				StoreDTO store = storeService.getStoreByEmployee();
		return ResponseEntity.ok(store);
	}

	//update store by id
	@PutMapping("/{id}")
	public ResponseEntity<StoreDTO> updateStore(@PathVariable Long id,
			@RequestBody StoreDTO storeDTO) throws ResourceNotFoundException, UserException {
		return ResponseEntity.ok(storeService.updateStore(id, storeDTO));
	}

	//delete store
	@DeleteMapping()
	public ResponseEntity<ApiResponse> deletedStore() throws ResourceNotFoundException, UserException {
		storeService.deletedStore();
		return ResponseEntity.ok(new ApiResponse("store deleted successfully"));
	}

	
	@GetMapping("/{storeId}/employee/list")
	@PreAuthorize("hasAnyAuthority('ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
	public ResponseEntity<List<UserDTO>> getStoreEmployeeList(@PathVariable Long storeId) throws UserException{
		List<UserDTO> employees = storeService.getEmployeesByStore(storeId);
		return ResponseEntity.ok(employees);
	} 

	@PostMapping("/add/employee")
	@PreAuthorize("hasAnyAuthority('STORE_MANAGER','STORE_ADMIN')")
	public ResponseEntity<UserDTO> addEmployeeToStore(@RequestBody UserDTO userDTO) throws UserException{
		UserDTO addedEmployee = storeService.addEmployee(null, userDTO);
		return ResponseEntity.ok(addedEmployee);
	}

}
