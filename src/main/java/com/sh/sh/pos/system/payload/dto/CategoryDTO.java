  package com.sh.sh.pos.system.payload.dto;

import com.sh.sh.pos.system.model.Store;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDTO {
	
	 
	private Long id;
	private String name;
	private Store store;
}
