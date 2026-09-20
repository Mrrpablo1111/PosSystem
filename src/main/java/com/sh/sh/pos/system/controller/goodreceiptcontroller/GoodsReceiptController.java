package com.sh.sh.pos.system.controller.goodreceiptcontroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptCreateDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptDashboardDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptResponseDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptUpdateDTO;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.goodreceiptservice.GoodsReceiptService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/goods-receipts")
@RequiredArgsConstructor
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    // ── Reads ─────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_VIEW')")
    public ResponseEntity<ApiResponse<List<GoodsReceiptResponseDTO>>> getAll(
            @RequestParam(required = false) Long purchaseOrderId) {
        List<GoodsReceiptResponseDTO> data = purchaseOrderId != null
            ? goodsReceiptService.getGoodsReceiptsByPurchaseOrder(purchaseOrderId)
            : goodsReceiptService.getAllGoodsReceipts();
        return ResponseEntity.ok(ApiResponse.success("Goods receipts fetched", data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_VIEW')")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Goods receipt fetched", goodsReceiptService.getGoodsReceiptById(id)));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_REPORT')")
    public ResponseEntity<ApiResponse<GoodsReceiptDashboardDTO>> getDashboard() {
        return ResponseEntity.ok(
            ApiResponse.success("Dashboard fetched", goodsReceiptService.getDashboard()));
    }

    // ── Draft lifecycle ───────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_CREATE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> create(
            @Valid @RequestBody GoodsReceiptCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Goods receipt created", goodsReceiptService.createGoodsReceipt(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_UPDATE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody GoodsReceiptUpdateDTO dto) {
        return ResponseEntity.ok(
            ApiResponse.success("Goods receipt updated", goodsReceiptService.updateGoodsReceipt(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        goodsReceiptService.deleteGoodsReceipt(id);
        return ResponseEntity.ok(ApiResponse.success("Goods receipt deleted", null));
    }

    // ── Status transitions ────────────────────────────────────────────────

    /** DRAFT → RECEIVING */
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_UPDATE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> receive(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Goods receipt is now receiving", goodsReceiptService.startReceiving(id)));
    }

    /**
     * DRAFT/RECEIVING → COMPLETED — the approval step.
     * Only Warehouse Manager / Store Admin / Super Admin roles hold
     * GOODS_RECEIPT_APPROVE; Warehouse Staff can draft but not approve.
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_APPROVE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Goods receipt completed", goodsReceiptService.completeGoodsReceipt(id)));
    }

    /** DRAFT/RECEIVING → CANCELLED */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_UPDATE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDTO>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Goods receipt cancelled", goodsReceiptService.cancelGoodsReceipt(id)));
    }
}
