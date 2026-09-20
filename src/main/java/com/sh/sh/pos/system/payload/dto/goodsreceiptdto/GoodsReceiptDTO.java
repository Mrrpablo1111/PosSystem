package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Full read model for a single Goods Receipt (detail views). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptDTO {

    private Long   id;
    private String grnNumber;
    private String status;

    // ── Purchase order ────────────────────────────────────────────────────
    private Long   purchaseOrderId;
    private String poNumber;

    // ── Parties / locations (resolved names for display) ──────────────────
    private Long   supplierId;
    private String supplierName;
    private Long   warehouseId;
    private String warehouseName;
    private Long   branchId;
    private String branchName;

    // ── Receiving info ────────────────────────────────────────────────────
    private LocalDate receivedDate;
    private Long      receivedById;
    private String    receivedByName;
    private String    note;

    // ── Aggregates (computed in mapper) ───────────────────────────────────
    private Integer totalReceivedQuantity;
    private Integer totalAcceptedQuantity;
    private Integer totalDamagedQuantity;
    private Integer totalRejectedQuantity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<GoodsReceiptItemDTO> items;
}
