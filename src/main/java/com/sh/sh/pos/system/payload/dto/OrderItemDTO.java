package com.sh.sh.pos.system.payload.dto;


import java.math.BigDecimal;

import com.sh.sh.pos.system.payload.dto.productDTO.ProductDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDTO {
	private Long id;
	
	private Integer quantity;
	
	private BigDecimal price;
	
	
	private ProductDTO product;
	
	private Long productId;
	
	private Long orderId;  
	
	
}
  