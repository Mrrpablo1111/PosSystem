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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.CategoryDTO;
import com.sh.sh.pos.system.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")

public class CategoryController {
	private final CategoryService categoryService;
	
	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
	public ResponseEntity<CategoryDTO> createCategory(
			@RequestBody CategoryDTO categroyDTO
			)throws UserException{
		return ResponseEntity.ok(
				categoryService.createCategory(categroyDTO));
		
	}
	
	@GetMapping("/store/{storeId}")
	public ResponseEntity<List<CategoryDTO>> getCategoriesByStore(
			@PathVariable Long storeId) throws Exception{
		return ResponseEntity.ok(
				categoryService.getCategoriesByStore(storeId)); 
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
	public ResponseEntity<CategoryDTO> updateCategory(
			@RequestBody CategoryDTO categoryDTO,
			@PathVariable Long id
			)throws UserException{
		
		return ResponseEntity.ok(
				categoryService.updateCategory(id, categoryDTO));
		
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
	public ResponseEntity<Void> deleteCategory(
		@PathVariable Long id) throws UserException{
		categoryService.deleteCategory(id);
		return ResponseEntity.noContent().build();
		}
		
	
	
	
	
}


