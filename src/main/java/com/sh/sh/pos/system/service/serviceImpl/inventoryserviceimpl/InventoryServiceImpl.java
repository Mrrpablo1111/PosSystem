package com.sh.sh.pos.system.service.serviceimpl.inventoryserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.mapper.inventorymapper.InventoryMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.payload.dto.inventorydto.InventoryDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.inventoryrepository.InventoryRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.service.inventoryservice.InventoryService;
import com.sh.sh.pos.system.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository inventoryRepository;
	private final BranchRepository branchRepository;
	private final ProductRepository productRepository;
	private final SecurityUtil securityUtil;

	@Override
	@Transactional
	public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
		Branch branch = branchRepository.findById(inventoryDTO.getBranchId()).orElseThrow(
				() -> new ResourceNotFoundException("Branch not found with id: " + inventoryDTO.getBranchId()));
		Product product = productRepository.findById(inventoryDTO.getProductId()).orElseThrow(
				() -> new ResourceNotFoundException("Product not found with id: " + inventoryDTO.getProductId()));

		securityUtil.checkAuthority(branch);

		Inventory inventory = InventoryMapper.toEntity(inventoryDTO, branch, product);
		return InventoryMapper.toDTO(inventoryRepository.save(inventory));
	}

	@Override
	@Transactional
	public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) {
		Inventory inventory = findInventoryOrThrow(id);

		securityUtil.checkAuthority(inventory);
		inventory.setQuantity(inventoryDTO.getQuantity());

		return InventoryMapper.toDTO(inventoryRepository.save(inventory));
	}

	@Override
	@Transactional
	public void deleteInventory(Long id) {
		Inventory inventory = findInventoryOrThrow(id);

		securityUtil.checkAuthority(inventory);
		inventoryRepository.delete(inventory);
	}

	@Override
	@Transactional(readOnly = true)
	public InventoryDTO getInventoryById(Long id) {
		return InventoryMapper.toDTO(findInventoryOrThrow(id));
	}

	@Override
	@Transactional(readOnly = true)
	public InventoryDTO getInventoryByProductId(Long productId) {
		return InventoryMapper.toDTO(inventoryRepository.findByProductId(productId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<InventoryDTO> getInventoryByBranch(Long branchId) {
		return inventoryRepository.findByBranchId(branchId)
				.stream()
				.map(InventoryMapper::toDTO)
				.toList();
	}

	// ── Helpers ───────────────────────────────────────────────────────────

	private Inventory findInventoryOrThrow(Long id) {
		return inventoryRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("Inventory not found with id: " + id));
	}
}