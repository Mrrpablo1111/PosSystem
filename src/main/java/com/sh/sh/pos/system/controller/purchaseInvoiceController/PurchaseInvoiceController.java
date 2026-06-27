package com.sh.sh.pos.system.controller.purchaseInvoiceController;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO.PurchaseInvoiceDTO;
import com.sh.sh.pos.system.service.purchaseInvoicesService.PurchaseInvoiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase-invoices")
@RequiredArgsConstructor
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService purchaseInvoiceService;

    @PostMapping
    public ResponseEntity<PurchaseInvoiceDTO> create(
            @RequestBody PurchaseInvoiceDTO dto) {

        PurchaseInvoiceDTO invoice = purchaseInvoiceService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseInvoiceDTO> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                purchaseInvoiceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseInvoiceDTO>> getAll() {

        return ResponseEntity.ok(
                purchaseInvoiceService.getAll());
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<String> postInvoice(
            @PathVariable Long id) {

        purchaseInvoiceService.post(id);

        return ResponseEntity.ok(
                "Purchase Invoice posted successfully.");
    }

}