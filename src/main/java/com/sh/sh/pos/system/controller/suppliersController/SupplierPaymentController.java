package com.sh.sh.pos.system.controller.suppliersController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierPaymentDTO;
import com.sh.sh.pos.system.service.suppliersService.SupplierPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/supplier-payments")
@RequiredArgsConstructor
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;

    @PostMapping
    public ResponseEntity<SupplierPaymentDTO> create(
            @RequestBody SupplierPaymentDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        supplierPaymentService.create(dto));
    }
}
