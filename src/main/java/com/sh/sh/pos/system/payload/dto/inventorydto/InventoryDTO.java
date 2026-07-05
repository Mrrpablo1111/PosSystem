package com.sh.sh.pos.system.payload.dto.inventorydto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.payload.dto.branchdto.BranchDTO;
import com.sh.sh.pos.system.payload.dto.productdto.ProductDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class InventoryDTO {

	private Long id;

	private BranchDTO branch;
	private Long branchId;

	private ProductDTO product;
	private Long productId;

	private Integer quantity;
	private Integer lowStockAlert;
	private LocalDateTime lastUpdate;

	private Integer reservedQuantity;

	private Integer availableQuantity;

	private boolean lowStock;

}
