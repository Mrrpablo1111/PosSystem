package com.sh.sh.pos.system.controller;

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

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.ProductDTO;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.ProductService;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
	private final ProductService productService;
	private final UserService userService;
	
	@PostMapping  
	public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO productDTO, @RequestHeader("Authorization") String jwt) throws Exception{
		User user = userService.getUserFromJwtToken(jwt);
		return ResponseEntity.ok(productService.createProduct(productDTO, null));
	}
	
	@GetMapping("/store/{storeId}")
	public ResponseEntity<List<ProductDTO>>getByStoreId(
			@PathVariable Long StoreId,
			@RequestHeader("Authorization") String jwt
			) throws Exception{
		return ResponseEntity.ok(productService.getProductByStoreId(StoreId));
				
	}
	
	@GetMapping("/store/{storeId}/search")
	public ResponseEntity<List<ProductDTO>> searchByKeyword(
			@PathVariable Long storeId,
			@RequestParam String keyword, 
			@RequestHeader("Authorization") String jwt){
		return ResponseEntity.ok(productService.searchBykeyword(storeId, keyword)); 
		
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<ProductDTO> update(
			@PathVariable Long id,
			@RequestBody ProductDTO productDTO,
			@RequestHeader("Authorization") String jwt
			) throws Exception{
			User user = userService.getUserFromJwtToken(jwt);
				return ResponseEntity.ok(
						productService.updateProduct(id, productDTO, user)
						);
		
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> delete(
			@PathVariable Long id,
			
			@RequestHeader("Authorization") String jwt
			) throws Exception{
		User user = userService.getUserFromJwtToken(jwt);
		
		productService.deleteProduct(id, user);
		
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMessage("Product deleted successfully"); 
		
				return ResponseEntity.ok(
						apiResponse
						);
		 
	}
}
