package com.sh.sh.pos.system.controller.inventorytransactioncontroller;



import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.inventorytransactiondto.InventoryTransactionDTO;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.inventoryservice.InventoryTransactionService;

import lombok.RequiredArgsConstructor;

/**
 * READ-ONLY on purpose. Transactions are written by the business modules
 * that cause them (Goods Receipt complete, sales, adjustments) inside their
 * own database transaction — never directly over HTTP. That keeps the ledger
 * trustworthy: every row traces back to a real document.
 */
@RestController
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<List<InventoryTransactionDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId) {

        List<InventoryTransactionDTO> data;
        if (productId != null && warehouseId != null) {
            data = transactionService.getByProductAndWarehouse(productId, warehouseId);
        } else if (productId != null) {
            data = transactionService.getByProduct(productId);
        } else if (warehouseId != null) {
            data = transactionService.getByWarehouse(warehouseId);
        } else {
            data = transactionService.getAll(page, size);
        }
        return ResponseEntity.ok(ApiResponse.success("Inventory transactions fetched", data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<InventoryTransactionDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Inventory transaction fetched", transactionService.getById(id)));
    }

    /** Movement trail for one document, e.g. /reference/GOODS_RECEIPT/42 */
    @GetMapping("/reference/{referenceType}/{referenceId}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<List<InventoryTransactionDTO>>> getByReference(
            @PathVariable String referenceType,
            @PathVariable Long referenceId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory transactions fetched",
            transactionService.getByReference(referenceType.toUpperCase(), referenceId)));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<List<InventoryTransactionDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Inventory transactions fetched",
            transactionService.getByDateRange(from, to)));
    }

    /** Ledger-derived stock — audit check against the Inventory table */
    @GetMapping("/net-quantity")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<Long>> getNetQuantity(
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.success("Net quantity computed",
            transactionService.getNetQuantity(productId, variantId, warehouseId)));
    }
}
