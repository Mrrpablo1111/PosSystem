package com.sh.sh.pos.system.controller.warehousesController;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sh.sh.pos.system.payload.dto.warehousesDTO.WarehouseDTO;
import com.sh.sh.pos.system.service.warehousesService.WarehouseService;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<WarehouseDTO> create(
            @RequestBody WarehouseDTO dto
    ) {
        return ResponseEntity.ok(
                warehouseService.create(dto)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseDTO> update(
            @PathVariable Long id,
            @RequestBody WarehouseDTO dto
    ) {
        return ResponseEntity.ok(
                warehouseService.update(id, dto)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDTO> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                warehouseService.getById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<WarehouseDTO>> getAll() {
        return ResponseEntity.ok(
                warehouseService.getAll()
        );
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<WarehouseDTO>> getByBranch(
            @PathVariable Long branchId
    ) {
        return ResponseEntity.ok(
                warehouseService.getByBranch(branchId)
        );
    }

    @GetMapping("/branch/{branchId}/default")
    public ResponseEntity<WarehouseDTO> getDefaultWarehouse(
            @PathVariable Long branchId
    ) {
        return ResponseEntity.ok(
                warehouseService.getDefaultWarehouse(branchId)
        );
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id
    ) {
        warehouseService.deactivate(id);
        return ResponseEntity.ok("Warehouse deactivated successfully");
    }
}