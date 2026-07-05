package com.sh.sh.pos.system.mapper.inventorymapper;

import com.sh.sh.pos.system.mapper.productmapper.ProductMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.payload.dto.inventorydto.InventoryDTO;

public class InventoryMapper {
	private InventoryMapper() {}
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
   