package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.model.StoreContact;

import lombok.Data;


@Data

public class StoreDTO {
	
	private Long id;
	
	private String brand;
	
	
	private UserDTO  storeAdmin;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	private String description;
	
	private String storeType; 
	
	private StoreStatus status;
	
	private StoreContact contact ; 
	

}
