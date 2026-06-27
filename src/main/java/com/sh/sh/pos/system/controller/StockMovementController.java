package com.sh.sh.pos.system.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.payload.dto.StockMovementDTO;
import com.sh.sh.pos.system.service.StockMovementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {
     private final StockMovementService stockMovementService;

    @PostMapping
    public ResponseEntity<StockMovementDTO> create(@RequestBody StockMovementDTO dto) {
        return ResponseEntity.ok(stockMovementService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMovementDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(stockMovementService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<StockMovementDTO>> getAll() {
        return ResponseEntity.ok(stockMovementService.getAll());
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<StockMovementDTO>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(stockMovementService.getByBranch(branchId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovementDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.getByProduct(productId));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<StockMovementDTO>> getByVariant(@PathVariable Long variantId) {
        return ResponseEntity.ok(stockMovementService.getByVariant(variantId));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<StockMovementDTO>> getByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(stockMovementService.getByBatch(batchId));
    }

    @GetMapping("/type/{movementType}")
    public ResponseEntity<List<StockMovementDTO>> getByType(
            @PathVariable StockMovementType movementType) {
        return ResponseEntity.ok(stockMovementService.getByType(movementType));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<StockMovementDTO>> getByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        return ResponseEntity.ok(stockMovementService.getByDateRange(start, end));
    }
}
