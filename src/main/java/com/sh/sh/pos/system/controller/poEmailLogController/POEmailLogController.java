package com.sh.sh.pos.system.controller.poEmailLogController;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.model.poEmailLogs.POEmailLog;
import com.sh.sh.pos.system.payload.dto.poEmailLogs.POEmailLogDTO;
import com.sh.sh.pos.system.repository.poEmailLogRepository.POEmailLogRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class POEmailLogController {

    private final POEmailLogRepository emailLogRepository;

    /**
     * GET /api/purchase-orders/{id}/emails
     * Email history for one PO — used in the PO detail drawer.
     *
     * Example response:
     * [
     *   { "id": 1, "emailType": "PURCHASE_ORDER", "sentTo": "supplier@email.com",
     *     "status": "SUCCESS", "sentAt": "2026-06-26T08:00:00" },
     *   { "id": 2, "emailType": "GOODS_RECEIVED", "sentTo": "supplier@email.com",
     *     "status": "SUCCESS", "sentAt": "2026-06-28T10:30:00" }
     * ]
     */
    @GetMapping("/{id}/emails")
    public ResponseEntity<List<POEmailLogDTO>> getEmailsByPO(
            @PathVariable Long id) {

        List<POEmailLogDTO> logs = emailLogRepository
                .findByPurchaseOrderIdOrderBySentAtDesc(id)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/purchase-orders/store/{storeId}/emails
     * All emails sent for a store's POs — used in the email history page.
     */
    @GetMapping("/store/{storeId}/emails")
    public ResponseEntity<List<POEmailLogDTO>> getEmailsByStore(
            @PathVariable Long storeId) {

        List<POEmailLogDTO> logs = emailLogRepository
                .findByPurchaseOrderBranchStoreIdOrderBySentAtDesc(storeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(logs);
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private POEmailLogDTO toDTO(POEmailLog log) {
        return POEmailLogDTO.builder()
                .id(log.getId())
                .purchaseOrderId(log.getPurchaseOrder() != null
                        ? log.getPurchaseOrder().getId()       : null)
                .poNumber(log.getPurchaseOrder() != null
                        ? log.getPurchaseOrder().getPoNumber() : null)
                .emailType(log.getEmailType() != null
                        ? log.getEmailType().name() : null)
                .sentTo(log.getSentTo())
                .subject(log.getSubject())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .sentAt(log.getSentAt())
                .build();
    }
}