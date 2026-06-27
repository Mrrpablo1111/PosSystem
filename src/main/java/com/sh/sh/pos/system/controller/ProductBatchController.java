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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.productDTO.ProductBatchDTO;
import com.sh.sh.pos.system.service.ProductService.ProductBatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product-batches")
@RequiredArgsConstructor
public class ProductBatchController {
    private final ProductBatchService batchService;

    @PostMapping
    public ResponseEntity<ProductBatchDTO> create(@RequestBody ProductBatchDTO dto) {
        return ResponseEntity.ok(batchService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBatchDTO> update(
            @PathVariable Long id,
            @RequestBody ProductBatchDTO dto) {
        return ResponseEntity.ok(batchService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductBatchDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        batchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductBatchDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(batchService.getByProduct(productId));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ProductBatchDTO>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(batchService.getByBranch(branchId));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<ProductBatchDTO>> getBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(batchService.getBySupplier(supplierId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductBatchDTO>> searchByBatchNo(
            @RequestParam String batchNo) {
        return ResponseEntity.ok(batchService.searchByBatchNo(batchNo));
    }

   @GetMapping("/expired")
public ResponseEntity<List<ProductBatchDTO>> getExpired() {
    return ResponseEntity.ok(batchService.getExpiredBatches());
}

@GetMapping("/expiring")
public ResponseEntity<List<ProductBatchDTO>> getExpiring(@RequestParam(defaultValue = "7") int days) {
    return ResponseEntity.ok(batchService.getExpiringBatches(days));
}
}
