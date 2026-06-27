package com.sh.sh.pos.system.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductDTO;
import com.sh.sh.pos.system.service.UserService;
import com.sh.sh.pos.system.service.ProductService.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
	private final ProductService productService;
	private final UserService userService;

	@PostMapping
	public ResponseEntity<ProductDTO> create(@RequestBody @Valid ProductDTO productDTO,
			@RequestHeader("Authorization") String jwt) throws UserException, AccessDeniedException {
		User user = userService.getUserFromJwtToken(jwt);
		return ResponseEntity.ok(productService.createProduct(productDTO, user));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getProductById(id));
	}

	@GetMapping("/store/{storeId}")
	public ResponseEntity<List<ProductDTO>> getByStoreId(
			@PathVariable Long storeId) {
		return ResponseEntity.ok(productService.getProductByStoreId(storeId));

	}

	@GetMapping("/store/{storeId}/search")
	public ResponseEntity<List<ProductDTO>> searchByKeyword(
			@PathVariable Long storeId,
			@RequestParam("query") String keyword) {
		return ResponseEntity.ok(productService.searchBykeyword(storeId, keyword));

	}

	@PatchMapping("/{id}")
	public ResponseEntity<ProductDTO> update(
			@PathVariable Long id,
			@RequestBody ProductDTO productDTO,
			@RequestHeader("Authorization") String jwt) throws Exception {
		User user = userService.getUserFromJwtToken(jwt);
		return ResponseEntity.ok(
				productService.updateProduct(id, productDTO, user));

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@PathVariable Long id,

			@RequestHeader("Authorization") String jwt) throws UserException, AccessDeniedException {
		User user = userService.getUserFromJwtToken(jwt);

		productService.deleteProduct(id, user);
		return ResponseEntity.noContent().build();

	}

	@GetMapping("/barcode/{barcode}")
	public ResponseEntity<ProductDTO> getByBarcode(@PathVariable String barcode) {
		return ResponseEntity.ok(productService.getProductByBarcode(barcode));
	}

}
