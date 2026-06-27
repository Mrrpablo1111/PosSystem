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
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.productDTO.ProductVariantDTO;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.ProductService.ProductVariantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService variantService;

    @PostMapping
    public ResponseEntity<ProductVariantDTO> create(@RequestBody ProductVariantDTO dto) {
        return ResponseEntity.ok(variantService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> update(
            @PathVariable Long id,
            @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.ok(variantService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(variantService.get(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(variantService.getByProduct(productId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        variantService.delete(id);
        return ResponseEntity.ok(new ApiResponse(
                    "Product variant deleted successfully"));
    }
}
