package com.sh.sh.pos.system.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.InventoryDTO;

import com.sh.sh.pos.system.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventories")
public class InventoryController {
	
	private final InventoryService inventoryService;
	
	@PostMapping()
	@PreAuthorize("hasAuthority('STORE_MANAGER')")
	public ResponseEntity<InventoryDTO> create(@RequestBody InventoryDTO inventoryDTO) throws AccessDeniedException, UserException{
		return ResponseEntity.ok(inventoryService.createInventory(inventoryDTO));
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('STORE_MANAGER')")
	public ResponseEntity<InventoryDTO> update(@RequestBody InventoryDTO inventoryDTO, @PathVariable Long id) throws AccessDeniedException, UserException{
		return ResponseEntity.ok(inventoryService.updateInventory(id, inventoryDTO));
	}
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('STORE_MANAGER')")
	public ResponseEntity<Void> delete(@PathVariable Long id) throws AccessDeniedException, UserException{
		inventoryService.deleteInventory(id);
		
	
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('STORE_MANAGER')")
	public ResponseEntity<InventoryDTO> getById(@PathVariable Long id){
		return ResponseEntity.ok(inventoryService.getInventoryById(id));
	}
	
	@GetMapping("/product/{productId}")
	public ResponseEntity<InventoryDTO> getInventoryByProduct(@PathVariable Long productId){
		return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
	}
	
	@GetMapping("/branch/{branchId}")
	public ResponseEntity<List<InventoryDTO>> getInventoryByBranch(@PathVariable Long branchId) throws Exception{
		return ResponseEntity.ok(inventoryService.getInventoryByBranch(branchId));
	}
	
	
}
