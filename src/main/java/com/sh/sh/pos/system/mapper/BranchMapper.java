package com.sh.sh.pos.system.mapper;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.BranchType;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.payload.dto.BranchDTO;

public class BranchMapper {
	public static BranchDTO toDTO(Branch branch) {
		return BranchDTO.builder()
				.id(branch.getId())
				.name(branch.getName())
				.address(branch.getAddress())
				.branchType(branch.getBranchType())
				.phone(branch.getPhone())
				.email(branch.getEmail())
				.closeTime(branch.getCloseTime())
				.openTime(branch.getOpenTime())
				.workingDays(branch.getWorkingDays())
				.storeId(branch.getStore() != null ? branch.getStore().getId() : null)
				.createdAt(branch.getCreatedAt())
				.updatedAt(branch.getUpdatedAt())
				.build();

	}

	public static Branch toEntity(BranchDTO branchDTO, Store store) {

		 Branch branch = new Branch();

        branch.setName(branchDTO.getName());

        branch.setBranchType(
                branchDTO.getBranchType() != null
                        ? branchDTO.getBranchType()
                        : BranchType.STORE
        );

        branch.setAddress(branchDTO.getAddress());
        branch.setPhone(branchDTO.getPhone());
        branch.setEmail(branchDTO.getEmail());
        branch.setWorkingDays(branchDTO.getWorkingDays());
        branch.setOpenTime(branchDTO.getOpenTime());
        branch.setCloseTime(branchDTO.getCloseTime());
        branch.setStore(store);

		return branch;

	}
}
