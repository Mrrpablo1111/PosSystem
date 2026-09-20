package com.sh.sh.pos.system.controller.inventorycontroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.inventorydto.InventoryDTO;
import com.sh.sh.pos.system.service.inventoryservice.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    // ═══════════════════════════════════════════════════════════════════
    // Existing endpoints — unchanged
    // ═══════════════════════════════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasAuthority('STORE_MANAGER')")
    public ResponseEntity<InventoryDTO> create(@RequestBody InventoryDTO inventoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(inventoryDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")
    public ResponseEntity<InventoryDTO> update(@RequestBody InventoryDTO inventoryDTO,
                                               @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, inventoryDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")
    public ResponseEntity<InventoryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    /** Now returns a list — one product can sit in several warehouses */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryDTO>> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<InventoryDTO>> getInventoryByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getInventoryByBranch(branchId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryDTO>> getInventoryByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId));
    }

    @GetMapping("/branch/{branchId}/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStock(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getLowStockByBranch(branchId));
    }

    // ═══════════════════════════════════════════════════════════════════
    // NEW — quantity mutators. Kept as PATCH (partial, targeted change)
    // rather than folding into the existing PUT, since these are narrow
    // operations (one number changes) with their own validation rules
    // (insufficient stock, over-reservation) distinct from a full record
    // edit. Returns the DTO (not the raw entity) to stay consistent with
    // every other endpoint on this controller.
    //
    // ADAPT: authority — STORE_MANAGER is a placeholder for all four;
    // reserve/release in particular may need to be reachable by cashier-role
    // staff at the POS register (reserving stock mid-sale), not just managers.
    // ═══════════════════════════════════════════════════════════════════

    @PatchMapping("/{id}/increase")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")
    public ResponseEntity<InventoryDTO> increaseStock(@PathVariable Long id,
                                                       @RequestParam int quantity) {
        inventoryService.increaseStock(id, quantity);
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @PatchMapping("/{id}/decrease")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")
    public ResponseEntity<InventoryDTO> decreaseStock(@PathVariable Long id,
                                                       @RequestParam int quantity) {
        inventoryService.decreaseStock(id, quantity);
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @PatchMapping("/{id}/reserve")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")   // ADAPT: consider allowing cashier role for POS-time reservation
    public ResponseEntity<InventoryDTO> reserveStock(@PathVariable Long id,
                                                      @RequestParam int quantity) {
        inventoryService.reserveStock(id, quantity);
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @PatchMapping("/{id}/release")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")   // ADAPT: same as above
    public ResponseEntity<InventoryDTO> releaseReservedStock(@PathVariable Long id,
                                                              @RequestParam int quantity) {
        inventoryService.releaseReservedStock(id, quantity);
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @PatchMapping("/{id}/reorder-level")
    @PreAuthorize("hasAuthority('STORE_MANAGER')")
    public ResponseEntity<InventoryDTO> updateReorderLevel(@PathVariable Long id,
                                                            @RequestParam int reorderLevel) {
        inventoryService.updateReorderLevel(id, reorderLevel);
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @GetMapping("/{id}/available")
    public ResponseEntity<Integer> getAvailableQuantity(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getAvailableQuantity(id));
    }
}
