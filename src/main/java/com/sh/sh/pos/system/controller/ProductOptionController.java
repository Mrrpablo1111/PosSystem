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

import com.sh.sh.pos.system.payload.dto.productDTO.ProductOptionDTO;
import com.sh.sh.pos.system.service.ProductService.ProductOptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product-options")
@RequiredArgsConstructor
public class ProductOptionController {
    private final ProductOptionService optionService;

    @PostMapping
    public ResponseEntity<ProductOptionDTO> create(@RequestBody ProductOptionDTO dto) {
        return ResponseEntity.ok(optionService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductOptionDTO> update(
            @PathVariable Long id,
            @RequestBody ProductOptionDTO dto) {
        return ResponseEntity.ok(optionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        optionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductOptionDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(optionService.findByProduct(productId));
    }
}
