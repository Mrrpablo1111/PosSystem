package com.sh.sh.pos.system.controller.branchcontroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.branchdto.BranchDTO;
import com.sh.sh.pos.system.service.branchservice.BranchService;
import com.sh.sh.pos.system.service.userservice.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {

	private final BranchService branchService;
	private final UserService userService;

	// Create a new branch
	@PostMapping
	public ResponseEntity<BranchDTO> createBranch(
			@RequestBody @Valid BranchDTO branchDTO,
			@RequestHeader("Authorization") String jwt) {

		User user = userService.getUserFromJwtToken(jwt);
		return ResponseEntity.ok(branchService.createBranch(branchDTO, user));
	}

	// Get a branch by ID
	@GetMapping("/{id}")
	public ResponseEntity<BranchDTO> getBranchById(@PathVariable Long id) {
		return ResponseEntity.ok(branchService.getBranchById(id));
	}

	// Get all branches by store ID
	@GetMapping("/store/{storeId}")
	public ResponseEntity<List<BranchDTO>> getAllBranchByStoreId(@PathVariable Long storeId) {
		return ResponseEntity.ok(branchService.getAllBranchesByStoreId(storeId));
	}

	// Update a branch
	@PutMapping("/{id}")
	public ResponseEntity<BranchDTO> updateBranch(
			@PathVariable Long id,
			@RequestBody BranchDTO branchDTO,
			@RequestHeader("Authorization") String jwt) throws UserException, ResourceNotFoundException {

		User user = userService.getUserFromJwtToken(jwt);
		return ResponseEntity.ok(branchService.updateBranch(id, branchDTO, user));
	}

	// Delete a branch
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBranchById(@PathVariable Long id) {
		branchService.deleteBranch(id);
		return ResponseEntity.noContent().build();
	}
}