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
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.StoreMapper;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.StoreService;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {
	private final StoreService storeService;
	private final UserService userService;

	@PostMapping("/create")
	public ResponseEntity<StoreDTO> createStore(@RequestBody StoreDTO storeDTO,
			@RequestHeader("Authorization") String jwt) throws UserException {
		User user = userService.getUserFromJwtToken(jwt);

		return ResponseEntity.ok(storeService.createStore(storeDTO, user));
	}

	@GetMapping("/{id}")
	public ResponseEntity<StoreDTO> getStoreById(@RequestHeader("Authorization") String jwt, @PathVariable Long id)
			throws Exception {
		return ResponseEntity.ok(storeService.getStoreById(id));
	}

	@GetMapping()
	public ResponseEntity<List<StoreDTO>> getAllStores(@RequestHeader("Authorization") String jwt) {
		return ResponseEntity.ok(storeService.getAllStores());
	}

	@GetMapping("/admin")
	public ResponseEntity<StoreDTO> getStoreByAdmin(@RequestHeader("Authorization") String jwt) throws UserException {
		return ResponseEntity.ok(StoreMapper.toDTO(storeService.getStoreByAdmin()));

	}

	@GetMapping("/employee")
	
	public ResponseEntity<StoreDTO> getStoreByEmployee(@RequestHeader("Authorization") String jwt)
			throws UserException {
		return ResponseEntity.ok(storeService.getStoreByEmployee());
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_STORE_ADMIN')")  
	public ResponseEntity<StoreDTO> updateStore(@PathVariable Long id, @RequestHeader("Authorization") String jwt,
			@RequestBody StoreDTO storeDTO) throws UserException {
		return ResponseEntity.ok(storeService.updateStore(id, storeDTO));
	}

	@PutMapping("/{id}/moderate")
	public ResponseEntity<StoreDTO> moderateStore(@PathVariable Long id, @RequestParam StoreStatus status,
			@RequestHeader("Authorization") String jwt, @RequestBody StoreDTO storeDTO) throws Exception {
		return ResponseEntity.ok(storeService.moderateStore(id, status));
	}

	@DeleteMapping  ("/{id}")
	public ResponseEntity<ApiResponse> deletedStore(@PathVariable Long id) throws UserException {
		storeService.deletedStore(id);
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMessage("store deleted successfully");
		return ResponseEntity.ok(apiResponse);
	}

}
