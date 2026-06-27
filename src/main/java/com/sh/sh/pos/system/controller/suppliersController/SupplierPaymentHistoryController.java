package com.sh.sh.pos.system.controller.suppliersController;



import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierPaymentHistoryDTO;
import com.sh.sh.pos.system.service.suppliersService.SupplierPaymentHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/supplier-payments")
@RequiredArgsConstructor
public class SupplierPaymentHistoryController {

    private final SupplierPaymentHistoryService historyService;

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<SupplierPaymentHistoryDTO>>
            getBySupplier(@PathVariable Long supplierId) {

        return ResponseEntity.ok(
                historyService.getBySupplier(supplierId));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<SupplierPaymentHistoryDTO>>
            getByInvoice(@PathVariable Long invoiceId) {

        return ResponseEntity.ok(
                historyService.getByInvoice(invoiceId));
    }
}