package com.sh.sh.pos.system.payload.dto.inventorydto;



import java.time.LocalDateTime;

import com.sh.sh.pos.system.payload.dto.branchdto.BranchDTO;
import com.sh.sh.pos.system.payload.dto.productdto.ProductDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor      // REQUIRED — without it Jackson cannot deserialize
@AllArgsConstructor     // @RequestBody InventoryDTO and POST/PUT fail
public class InventoryDTO {

    private Long id;

    private BranchDTO branch;
    private Long branchId;

    private ProductDTO product;
    private Long productId;

    // ── Warehouse + variant scope ─────────────────────────────────────────
    private Long warehouseId;
    private String warehouseName;
    private Long variantId;
    private String variantName;

    private Integer quantity;
    private Integer lowStockAlert;
    private Integer reorderLevel;
    private LocalDateTime lastUpdate;

    private Integer reservedQuantity;
    private Integer availableQuantity;
    private boolean lowStock;

    /** Optional — shown on manual adjustments, stored in the transaction ledger */
    private String adjustmentNote;
}
