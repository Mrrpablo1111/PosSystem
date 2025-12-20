package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;

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
	
	private LocalDateTime lastUpdate;


	
}
