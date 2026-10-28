package com.sh.sh.pos.system.controller.storecontroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.storemapper.StoreMapper;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.storedto.StoreDTO;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.storeservice.StoreService;
import com.sh.sh.pos.system.service.userservice.UserService;

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
	@PostMapping
	public ResponseEntity<StoreDTO> createStore(@Valid @RequestBody StoreDTO storeDTO,
			@RequestHeader("Authorization") String jwt) throws UserException {

		User currentUser = userService.getUserFromJwtToken(jwt);

		return ResponseEntity.ok(storeService.createStore(storeDTO,currentUser));
	}

	// Get Store By ID
	@GetMapping("/{id}")
	public ResponseEntity<StoreDTO> getStoreById(@PathVariable Long id)
			throws UserException {
		return ResponseEntity.ok(storeService.getStoreById(id));
	}
	@PatchMapping("/{id}/deactivate")
	public ResponseEntity<Void> deactivateStore(@PathVariable Long id) throws UserException {
		storeService.deactivateStore(id);
		return ResponseEntity.noContent().build();
	}
	//Get Store By admin user ID
	@GetMapping("/admin")
	public ResponseEntity<StoreDTO> getStoreByAdmin() throws UserException {

		return ResponseEntity.ok(storeService.getStoreByAdminId());
	}

	// Get all stores(admin)
	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<List<StoreDTO>> getAllStores(@RequestParam(required = false) StoreStatus status) throws UserException {
		return ResponseEntity.ok(storeService.getAllStores(status));
	}

	//Update a Store
	@PutMapping("/{id}")
	public ResponseEntity<StoreDTO> updateStore(@PathVariable Long id, @Valid @RequestBody StoreDTO storeDTO) throws UserException {
		return ResponseEntity.ok(storeService.updateStore(id, storeDTO));
	}



	@PutMapping("/{storeId}/moderate")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<StoreDTO> moderateStore(@PathVariable Long storeId, @RequestParam StoreStatus status
			) throws UserException {
		return ResponseEntity.ok(storeService.moderateStore(storeId, status));
	}

	// Get Store By employee user ID
	@GetMapping("/employee")
	public ResponseEntity<StoreDTO> getStoreByEmployee()
			throws UserException {
		return ResponseEntity.ok(storeService.getStoreByEmployee());
	}

	//List Employee belonging to particular store.
	@GetMapping("/{storeId}/employee/list")
	@PreAuthorize("hasAnyAuthority('ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
	public ResponseEntity<List<UserDTO>> getStoreEmployeeList(@PathVariable Long storeId) throws UserException{

		return ResponseEntity.ok(storeService.getEmployeesByStore(storeId));
	} 

	@PostMapping("/{storeId}/employees")
	@PreAuthorize(
			"hasAnyAuthority('ROLE_STORE_MANAGER', " +
					"'ROLE_STORE_ADMIN', 'ROLE_ADMIN')"
	)
	public ResponseEntity<UserDTO> addEmployeeToStore(@PathVariable Long storeId, @Valid @RequestBody UserDTO userDTO ) throws UserException{
		UserDTO addedEmployee = storeService.addEmployee(storeId, userDTO);
		return ResponseEntity.ok(addedEmployee);
	}

}
