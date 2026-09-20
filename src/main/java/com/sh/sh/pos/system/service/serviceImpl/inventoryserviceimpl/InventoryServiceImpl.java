package com.sh.sh.pos.system.service.serviceimpl.inventoryserviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.inventoryexception.InventoryNotFoundException;
import com.sh.sh.pos.system.mapper.inventorymapper.InventoryMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.payload.dto.inventorydto.InventoryDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.inventoryrepository.InventoryRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.warehousesrepository.WarehouseRepository;
import com.sh.sh.pos.system.service.inventoryservice.InventoryCalculationService;
import com.sh.sh.pos.system.service.inventoryservice.InventoryService;
import com.sh.sh.pos.system.service.inventoryservice.InventoryValidationService;
import com.sh.sh.pos.system.service.inventoryservice.event.InventoryLowStockEvent;
import com.sh.sh.pos.system.service.inventoryservice.event.InventoryUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final WarehouseRepository warehouseRepository;
    private final BranchRepository branchRepository;

    private final InventoryValidationService validationService;
    private final InventoryCalculationService calculationService;
    private final ApplicationEventPublisher eventPublisher;

    // ═══════════════════════════════════════════════════════════════════
    // CRUD — matches your existing InventoryController exactly
    // ═══════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public InventoryDTO createInventory(InventoryDTO dto) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new InventoryNotFoundException("Branch not found: " + dto.getBranchId()));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException("Product not found: " + dto.getProductId()));
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new InventoryNotFoundException("Warehouse not found: " + dto.getWarehouseId()));
        ProductVariant variant = dto.getVariantId() != null
                ? productVariantRepository.findById(dto.getVariantId()).orElse(null)
                : null;

        validationService.validateWarehouseActive(warehouse);
        validationService.validateProductActive(product);
        if (variant != null) validationService.validateVariantBelongsToProduct(variant, product);

        Inventory entity = InventoryMapper.toEntity(dto, branch, product, warehouse, variant);
        return InventoryMapper.toDTO(inventoryRepository.save(entity));
    }

    @Override
    @Transactional
    public InventoryDTO updateInventory(Long id, InventoryDTO dto) {
        Inventory existing = findByIdOrThrow(id);

        // Partial update — null/absent fields on the incoming DTO leave the
        // existing value untouched (the same pattern used across the rest
        // of this codebase, e.g. ProductServiceImpl.updateProduct).
        if (dto.getQuantity() != null) existing.setQuantity(dto.getQuantity());
        if (dto.getReservedQuantity() != null) existing.setReservedQuantity(dto.getReservedQuantity());
        if (dto.getReorderLevel() != null) existing.setReorderLevel(dto.getReorderLevel());
        if (dto.getLowStockAlert() != null) existing.setLowStockAlert(dto.getLowStockAlert());

        Inventory saved = inventoryRepository.save(existing);
        publishUpdateAndLowStockIfNeeded(saved);
        return InventoryMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        inventoryRepository.delete(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryById(Long id) {
        return InventoryMapper.toDTO(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductIdOrderByWarehouseIdAsc(productId)
                .stream().map(InventoryMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryByBranch(Long branchId) {
        return inventoryRepository.findByBranchId(branchId)
                .stream().map(InventoryMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryByWarehouse(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId)
                .stream().map(InventoryMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getLowStockByBranch(Long branchId) {
        return inventoryRepository.findLowStockByBranch(branchId)
                .stream().map(InventoryMapper::toDTO).toList();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Quantity mutators — entity-based, for internal business-logic callers
    // ═══════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventory> findInventory(Long productId, Long variantId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndProductVariantIdAndWarehouseId(
                productId, variantId, warehouseId);
    }

    @Override
    @Transactional
    public Inventory findOrCreateInventory(Long productId, Long variantId, Long warehouseId, Long branchId) {
        return findInventory(productId, variantId, warehouseId).orElseGet(() -> {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new InventoryNotFoundException("Product not found: " + productId));
            ProductVariant variant = variantId != null
                    ? productVariantRepository.findById(variantId).orElse(null) : null;
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new InventoryNotFoundException("Warehouse not found: " + warehouseId));
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new InventoryNotFoundException("Branch not found: " + branchId));

            validationService.validateWarehouseActive(warehouse);
            validationService.validateProductActive(product);
            if (variant != null) validationService.validateVariantBelongsToProduct(variant, product);

            Inventory inventory = Inventory.builder()
                    .product(product).productVariant(variant).warehouse(warehouse).branch(branch)
                    .quantity(0).reservedQuantity(0).reorderLevel(0)
                    .build();
            return inventoryRepository.save(inventory);
        });
    }

    @Override
    @Transactional
    public Inventory increaseStock(Long inventoryId, int quantity) {
        Inventory inventory = findByIdOrThrow(inventoryId);
        validationService.validateNotDeleted(inventory);

        inventory.setQuantity(inventory.getQuantity() + quantity);
        Inventory saved = inventoryRepository.save(inventory);
        publishUpdateAndLowStockIfNeeded(saved);
        return saved;
    }

    @Override
    @Transactional
    public Inventory decreaseStock(Long inventoryId, int quantity) {
        Inventory inventory = findByIdOrThrow(inventoryId);
        validationService.validateNotDeleted(inventory);
        validationService.validateSufficientStock(inventory.getQuantity(), quantity);

        inventory.setQuantity(inventory.getQuantity() - quantity);
        Inventory saved = inventoryRepository.save(inventory);
        publishUpdateAndLowStockIfNeeded(saved);
        return saved;
    }

    @Override
    @Transactional
    public Inventory reserveStock(Long inventoryId, int quantity) {
        Inventory inventory = findByIdOrThrow(inventoryId);
        int available = calculationService.getAvailableQuantity(
                inventory.getQuantity(), inventory.getReservedQuantity());
        validationService.validateReservationWithinAvailable(available, quantity);

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        Inventory saved = inventoryRepository.save(inventory);
        publishUpdateAndLowStockIfNeeded(saved);
        return saved;
    }

    @Override
    @Transactional
    public Inventory releaseReservedStock(Long inventoryId, int quantity) {
        Inventory inventory = findByIdOrThrow(inventoryId);
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        Inventory saved = inventoryRepository.save(inventory);
        publishUpdateAndLowStockIfNeeded(saved);
        return saved;
    }

    @Override
    @Transactional
    public Inventory updateReorderLevel(Long inventoryId, int reorderLevel) {
        Inventory inventory = findByIdOrThrow(inventoryId);
        inventory.setReorderLevel(reorderLevel);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableQuantity(Long inventoryId) {
        Inventory inventory = findByIdOrThrow(inventoryId);
        return calculationService.getAvailableQuantity(inventory.getQuantity(), inventory.getReservedQuantity());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Inventory findByIdOrThrow(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + id));
    }

    private void publishUpdateAndLowStockIfNeeded(Inventory inventory) {
        int available = calculationService.getAvailableQuantity(
                inventory.getQuantity(), inventory.getReservedQuantity());

        eventPublisher.publishEvent(new InventoryUpdatedEvent(
                inventory.getId(), inventory.getProduct().getId(),
                inventory.getWarehouse().getId(), inventory.getQuantity()));

        if (calculationService.isLowStock(available, inventory.getReorderLevel())) {
            eventPublisher.publishEvent(new InventoryLowStockEvent(
                    inventory.getId(), inventory.getProduct().getId(),
                    inventory.getWarehouse().getId(), available, inventory.getReorderLevel()));
        }
    }
}
