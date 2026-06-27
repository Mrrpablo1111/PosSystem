package com.sh.sh.pos.system.controller.purchasesOrderController;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.purchasesOrderDTO.PurchaseOrderDTO;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest;
import com.sh.sh.pos.system.payload.request.ReceiveGoodsRequestDTO;
import com.sh.sh.pos.system.payload.request.UpdateTrackingRequestDTO;
import com.sh.sh.pos.system.service.purchasesOrderService.PurchaseOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

      private final PurchaseOrderService purchaseOrderService;
 
    // ── Create ────────────────────────────────────────────────────────────
 
    /**
     * POST /api/purchase-orders
     * Creates a new purchase order with status DRAFT.
     */
    @PostMapping
    public ResponseEntity<PurchaseOrderDTO> create(
            @Valid @RequestBody CreatePurchaseOrderRequest req) {
 
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(purchaseOrderService.create(req));
    }
 
    // ── Read ──────────────────────────────────────────────────────────────
 
    /**
     * GET /api/purchase-orders/store/{storeId}
     * Returns all POs that belong to a store (via branch → store).
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<PurchaseOrderDTO>> getAllByStore(
            @PathVariable Long storeId) {
        return ResponseEntity.ok(purchaseOrderService.getAllByStore(storeId));
    }

     @GetMapping("/all")
    public ResponseEntity<List<PurchaseOrderDTO>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.getAll());
    }
 
    /**
     * GET /api/purchase-orders/{id}
     * Returns a single PO by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }
 
    // ── Status transitions ────────────────────────────────────────────────
 
    /**
     * PUT /api/purchase-orders/{id}/approve
     * Moves PO from DRAFT or PENDING → APPROVED.
     * Returns the updated PO.
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<PurchaseOrderDTO> approve(
            @PathVariable Long id) {
 
        return ResponseEntity.ok(
                purchaseOrderService.approve(id));
    }
 
    /**
     * PUT /api/purchase-orders/{id}/cancel
     * Cancels the PO. Not allowed on RECEIVED or CLOSED orders.
     * Returns the updated PO.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderDTO> cancel(
            @PathVariable Long id) {
 
        return ResponseEntity.ok(
                purchaseOrderService.cancel(id));
    }
 
    /**
     * PUT /api/purchase-orders/{id}/send
     * Sends the PO email to the supplier and moves status DRAFT → PENDING.
     * Returns the updated PO.
     */
    @PutMapping("/{id}/send")
    public ResponseEntity<PurchaseOrderDTO> sendToSupplier(
            @PathVariable Long id) {
 
        return ResponseEntity.ok(
                purchaseOrderService.sendToSupplier(id));
    }
 
    // ── Receive goods ─────────────────────────────────────────────────────
 
    /**
     * POST /api/purchase-orders/receive
     * Records received quantities per line item.
     * Supports partial delivery — only include items that arrived.
     * Automatically promotes PO status to PARTIALLY_RECEIVED or RECEIVED.
     */
    @PostMapping("/receive")
    public ResponseEntity<PurchaseOrderDTO> receiveGoods(
            @Valid @RequestBody ReceiveGoodsRequestDTO req) {
 
        return ResponseEntity.ok(
                purchaseOrderService.receiveGoods(req));
    }
 
    // ── Tracking ──────────────────────────────────────────────────────────
 
    /**
     * PATCH /api/purchase-orders/{id}/tracking
     * Updates tracking number and/or carrier after the supplier dispatches.
     * Pass null or empty trackingNo to clear it.
     * Not allowed on CLOSED or CANCELLED orders.
     */
    @PatchMapping("/{id}/tracking")
    public ResponseEntity<PurchaseOrderDTO> updateTracking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrackingRequestDTO req) {
 
        return ResponseEntity.ok(
                purchaseOrderService.updateTracking(id, req));
    }
}