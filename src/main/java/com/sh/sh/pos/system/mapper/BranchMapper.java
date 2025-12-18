package com.sh.sh.pos.system.mapper;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.payload.dto.BranchDTO;

public class BranchMapper {
	public static BranchDTO toDTO(Branch branch) {
		return BranchDTO.builder()
				.id(branch.getId())
				.name(branch.getName())
				.address(branch.getAddress())
				.phone(branch.getPhone())
				.email(branch.getEmail())
				.closeTime(branch.getCloseTime())
				.openTime(branch.getOpenTime())
				.workingDays(branch.getWorkingDays())
				.storeId(branch.getStore()!=null?branch.getStore().getId():null)
				.createdAt(branch.getCreatedAt())
				.updatedAt(branch.getUpdatedAt())
				.build();
				
	}
	
	public static Branch toEntity(BranchDTO branchDTO, Store store) {
		return Branch.builder()
				.name(branchDTO.getName())
				.store(store)
				.address(branchDTO.getAddress())
				.phone(branchDTO.getPhone())
				.email(branchDTO.getEmail())
				.closeTime(branchDTO.getCloseTime())
				.openTime(branchDTO.getOpenTime())
				.workingDays(branchDTO.getWorkingDays())
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
				
	}
}
