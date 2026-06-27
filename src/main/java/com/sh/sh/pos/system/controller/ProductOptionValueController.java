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

import com.sh.sh.pos.system.payload.dto.productDTO.ProductOptionValueDTO;
import com.sh.sh.pos.system.service.ProductService.ProductOptionValueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product-option-values")
@RequiredArgsConstructor
public class ProductOptionValueController {
    private final ProductOptionValueService valueService;

    @PostMapping
    public ResponseEntity<ProductOptionValueDTO> create(@RequestBody ProductOptionValueDTO dto) {
        return ResponseEntity.ok(valueService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductOptionValueDTO> update(
            @PathVariable Long id,
            @RequestBody ProductOptionValueDTO dto) {
        return ResponseEntity.ok(valueService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        valueService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/option/{optionId}")
    public ResponseEntity<List<ProductOptionValueDTO>> getByOption(@PathVariable Long optionId) {
        return ResponseEntity.ok(valueService.findByOption(optionId));
    }
}
