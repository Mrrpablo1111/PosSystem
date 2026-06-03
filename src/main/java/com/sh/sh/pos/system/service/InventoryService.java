package com.sh.sh.pos.system.service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.InventoryDTO;

@Service
public interface InventoryService {
	InventoryDTO createInventory(InventoryDTO inventoryDTO) throws AccessDeniedException, UserException;
	InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) throws AccessDeniedException, UserException;
	void deleteInventory(Long id) throws AccessDeniedException, UserException;
	InventoryDTO getInventoryById(Long id);
	InventoryDTO getInventoryByProductId(Long productId);
	
	List<InventoryDTO> getInventoryByBranch(Long branchId);
}
