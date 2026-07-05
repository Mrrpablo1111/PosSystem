package com.sh.sh.pos.system.controller.supplierscontroller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierLedgerDTO;
import com.sh.sh.pos.system.service.suppliersservice.SupplierLedgerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierLedgerController {

    private final SupplierLedgerService supplierLedgerService;

    @GetMapping("/{supplierId}/ledger")
    public ResponseEntity<SupplierLedgerDTO> getLedger(
            @PathVariable Long supplierId) {

        return ResponseEntity.ok(
                supplierLedgerService.getLedger(supplierId));
    }
}