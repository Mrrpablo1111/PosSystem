package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Inventory;
import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.payload.dto.InventoryDTO;

public class InventoryMapper {
	
	public static InventoryDTO toDTO(Inventory inventory) {
		return InventoryDTO.builder()
				.id(inventory.getId())
				.branchId(inventory.getBranch().getId())
				.productId(inventory.getProduct().getId())
				.product(ProductMapper.toDTO(inventory.getProduct()))
				.quantity(inventory.getQuantity())
				.build();
	}
	
	public static Inventory toEntity(InventoryDTO inventoryDTO, Branch branch, Product product) {
		return Inventory.builder()
				.branch(branch)
				.product(product)
				.quantity(inventoryDTO.getQuantity())
				.build();
				
	}

}
   