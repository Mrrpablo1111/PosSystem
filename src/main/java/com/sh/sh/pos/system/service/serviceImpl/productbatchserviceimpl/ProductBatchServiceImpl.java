package com.sh.sh.pos.system.service.serviceimpl.productbatchserviceimpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.productbatchmapper.ProductBatchMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.payload.dto.productdto.ProductBatchDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.productbatchrepository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.productrepository.ProductRepository;
import com.sh.sh.pos.system.repository.productvariantrepository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.suppliersrepository.SupplierRepository;
import com.sh.sh.pos.system.repository.warehousesrepository.WarehouseRepository;
import com.sh.sh.pos.system.service.productservice.ProductBatchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl implements ProductBatchService {

    private final ProductBatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository variantRepository;
    private final WarehouseRepository warehouseRepository;

    // ── Create ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductBatchDTO create(ProductBatchDTO dto) {
        Product product = findProduct(dto.getProductId());
        Branch branch = findBranch(dto.getBranchId());
        Supplier supplier = resolveSupplier(dto.getSupplierId());
        ProductVariant variant = resolveVariant(dto.getVariantId());
        Warehouse warehouse = resolveWarehouse(dto.getWarehouseId(), branch);

        validateNoDuplicateBatch(warehouse.getId(), product.getId(), dto.getBatchNo(), null);

        ProductBatch batch = ProductBatchMapper.toEntity(dto, product, branch, supplier, variant, warehouse);
        return ProductBatchMapper.toDTO(batchRepository.save(batch));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProductBatchDTO get(Long id) {
        return ProductBatchMapper.toDTO(findBatch(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> getByProduct(Long productId) {
        return toDTOList(batchRepository.findByProductId(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> getByBranch(Long branchId) {
        return toDTOList(batchRepository.findByBranchId(branchId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> getBySupplier(Long supplierId) {
        return toDTOList(batchRepository.findBySupplierId(supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> searchByBatchNo(String batchNo) {
        return toDTOList(batchRepository.findByBatchNoContainingIgnoreCase(batchNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> getExpiredBatches() {
        LocalDate now = LocalDate.now();
        return toDTOList(batchRepository.findByExpiryDateBetween(LocalDate.of(2000, 1, 1), now.minusDays(1)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> getExpiringBatches(int days) {
        LocalDate now = LocalDate.now();
        return toDTOList(batchRepository.findByExpiryDateBetween(now, now.plusDays(days)));
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductBatchDTO update(Long id, ProductBatchDTO dto) {
        ProductBatch batch = findBatch(id);

        // Moving the batch to another warehouse is allowed within the same branch
        Warehouse warehouse = dto.getWarehouseId() != null
                ? resolveWarehouse(dto.getWarehouseId(), batch.getBranch())
                : batch.getWarehouse();

        // batchNo or warehouse changes must not collide with an existing lot
        validateNoDuplicateBatch(warehouse.getId(), batch.getProduct().getId(), dto.getBatchNo(), batch.getId());

        batch.setWarehouse(warehouse);
        batch.setBatchNo(dto.getBatchNo());
        batch.setPurchasePrice(dto.getPurchasePrice());
        batch.setExpiryDate(dto.getExpiryDate());
        applyQuantityCorrection(batch, dto.getQuantity());

        batch.setSupplier(resolveSupplier(dto.getSupplierId()));
        batch.setVariant(resolveVariant(dto.getVariantId()));
        batch.setExpiryDate(dto.getExpiryDate());
        batch.setManufactureDate(dto.getManufactureDate());
        return ProductBatchMapper.toDTO(batchRepository.save(batch));
    }

    /**
     * Manual quantity corrections (e.g. fixing a data-entry mistake) shift
     * remainingQuantity by the same delta, so already-sold stock is preserved.
     * Stock sold/returned through orders should go through stock movements,
     * not this method.
     */
    private void applyQuantityCorrection(ProductBatch batch, Integer newQuantity) {
        if (newQuantity == null || newQuantity.equals(batch.getQuantity())) {
            return;
        }
        int delta = newQuantity - batch.getQuantity();
        batch.setQuantity(newQuantity);
        batch.setRemainingQuantity(Math.max(0, batch.getRemainingQuantity() + delta));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        batchRepository.delete(findBatch(id));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<ProductBatchDTO> toDTOList(List<ProductBatch> batches) {
        return batches.stream().map(ProductBatchMapper::toDTO).toList();
    }

    /**
     * Resolves the warehouse for a batch:
     * - explicit warehouseId → must exist AND belong to the batch's branch
     * (prevents attaching a batch to another branch's warehouse)
     * - null warehouseId → falls back to the branch's default warehouse,
     * keeping older UI flows working that don't send a warehouse yet
     */
    private Warehouse resolveWarehouse(Long warehouseId, Branch branch) {
        if (warehouseId != null) {
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Warehouse not found with id: " + warehouseId));
            if (!warehouse.getBranch().getId().equals(branch.getId())) {
                throw new UserException(
                        "Warehouse " + warehouse.getName() + " does not belong to branch " + branch.getId() + ".");
            }
            return warehouse;
        }
        return warehouseRepository.findByBranchIdAndIsDefaultTrue(branch.getId())
                .orElseThrow(() -> new UserException(
                        "No warehouse specified and branch " + branch.getId()
                                + " has no default warehouse. Please select a warehouse."));
    }

    /**
     * Friendly 400 instead of a raw DB constraint violation on
     * (warehouse_id, product_id, batch_no). excludeId skips the batch being
     * updated so saving it unchanged doesn't collide with itself.
     */
    private void validateNoDuplicateBatch(Long warehouseId, Long productId, String batchNo, Long excludeId) {
        batchRepository.findByWarehouseIdAndProductIdAndBatchNo(warehouseId, productId, batchNo)
                .filter(existing -> !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new UserException(
                            "Batch " + batchNo + " already exists for this product in this warehouse. "
                                    + "Edit the existing batch instead of creating a duplicate.");
                });
    }

    private ProductBatch findBatch(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
    }

    private ProductVariant resolveVariant(Long variantId) {
        if (variantId == null) {
            return null;
        }
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));
    }
}