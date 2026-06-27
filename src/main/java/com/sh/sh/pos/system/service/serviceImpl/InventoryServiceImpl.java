package com.sh.sh.pos.system.service.serviceImpl;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.InventoryMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.payload.dto.InventoryDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.InventoryRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.service.InventoryService;
import com.sh.sh.pos.system.util.SecurityUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
	private final InventoryRepository inventoryRepository;
	private final BranchRepository branchRepository;
	private final ProductRepository productRepository;
	private final SecurityUtil securityUtil;

	@Override
	public InventoryDTO createInventory(InventoryDTO inventoryDTO) throws AccessDeniedException, UserException{
		Branch branch = branchRepository.findById(inventoryDTO.getBranchId()).orElseThrow(
				() -> new EntityNotFoundException("branch not exist ..."));
		Product product = productRepository.findById(inventoryDTO.getProductId()).orElseThrow(
				() -> new EntityNotFoundException("product not exist ..."));

		securityUtil.checkAuthority(branch);
		Inventory inventory = InventoryMapper.toEntity(inventoryDTO, branch, product);
		
		return InventoryMapper.toDTO(inventoryRepository.save(inventory));
	}

	@Override
	public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) throws AccessDeniedException, UserException {
		Inventory inventory = inventoryRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("inventory not found...")
				);

		securityUtil.checkAuthority(inventory);
		inventory.setQuantity(inventoryDTO.getQuantity());

		return InventoryMapper.toDTO(inventoryRepository.save(inventory));
	}

	@Override
	public void deleteInventory(Long id) throws AccessDeniedException, UserException {
		Inventory inventory = inventoryRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("inventory not found...")
				);

		securityUtil.checkAuthority(inventory);
		inventoryRepository.delete(inventory);
		
	}

	@Override
	public InventoryDTO getInventoryById(Long id) {
		Inventory inventory = inventoryRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("inventory not found...")
				);
		return InventoryMapper.toDTO(inventory);
	}

	@Override
	public InventoryDTO getInventoryByProductId(Long productId) {
		return InventoryMapper.toDTO(inventoryRepository.findByProductId(productId));
	}

	@Override
	public List<InventoryDTO> getInventoryByBranch(Long branchId) {
		return inventoryRepository.findByBranchId(branchId)
				.stream()
				.map(InventoryMapper::toDTO)
				.collect(Collectors.toList());
	}

}
