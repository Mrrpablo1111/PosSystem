package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.sh.sh.pos.system.domain.BranchType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BranchDTO {

	private Long id;
	private String name;
	private BranchType branchType;
	private String address;
	private String phone;
	private String email;
	private List<String> workingDays;
	private LocalTime openTime;
	private LocalTime closeTime;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private StoreDTO store;
	private Long storeId; 
	private UserDTO  manager;

	public BranchDTO(Long id, String name, String address){
		this.id = id;
		this.name = name;
		this.address = address;
	}
}
