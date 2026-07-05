package com.sh.sh.pos.system.controller.purchasesordercontroller;



import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.repository.purchasesorderrepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.service.purchaseorderspdfservice.PurchaseOrderPdfService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderPdfController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderPdfService pdfService;

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_MANAGER')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderRepository
                .findByIdWithAllRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        byte[] pdf = pdfService.generatePdf(order);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=PurchaseOrder-" + order.getPoNumber() + ".pdf")
                .body(pdf);
    }
}