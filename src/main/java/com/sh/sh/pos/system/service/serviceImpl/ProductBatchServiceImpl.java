package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.ProductBatchMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductBatchDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.repository.ProductVariantRepository;
import com.sh.sh.pos.system.repository.suppliersRepository.SupplierRepository;
import com.sh.sh.pos.system.service.ProductService.ProductBatchService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl implements ProductBatchService {

    private final ProductBatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository variantRepository;

    // ── Create ──────────────────────────────────────────────────────────────

    @Override
    public ProductBatchDTO create(ProductBatchDTO dto) {
        Product product = findProduct(dto.getProductId());
        Branch branch = findBranch(dto.getBranchId());
        Supplier supplier = resolveSupplier(dto.getSupplierId());
        ProductVariant variant = resolveVariant(dto.getVariantId());

        ProductBatch batch = ProductBatchMapper.toEntity(dto, product, branch, supplier, variant);
        return ProductBatchMapper.toDTO(batchRepository.save(batch));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    public ProductBatchDTO get(Long id) {
        return ProductBatchMapper.toDTO(findBatch(id));
    }

    @Override
    public List<ProductBatchDTO> getByProduct(Long productId) {
        return toDTOList(batchRepository.findByProductId(productId));
    }

    @Override
    public List<ProductBatchDTO> getByBranch(Long branchId) {
        return toDTOList(batchRepository.findByBranchId(branchId));
    }

    @Override
    public List<ProductBatchDTO> getBySupplier(Long supplierId) {
        return toDTOList(batchRepository.findBySupplierId(supplierId));
    }

    @Override
    public List<ProductBatchDTO> searchByBatchNo(String batchNo) {
        return toDTOList(batchRepository.findByBatchNoContainingIgnoreCase(batchNo));
    }

    @Override
    public List<ProductBatchDTO> getExpiredBatches() {
        LocalDate now = LocalDate.now();
        return toDTOList(batchRepository.findByExpiryDateBetween(LocalDate.of(2000, 1, 1), now.minusDays(1)));
    }

    @Override
    public List<ProductBatchDTO> getExpiringBatches(int days) {
        LocalDate now = LocalDate.now();
        return toDTOList(batchRepository.findByExpiryDateBetween(now, now.plusDays(days)));
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    public ProductBatchDTO update(Long id, ProductBatchDTO dto) {
        ProductBatch batch = findBatch(id);

        batch.setBatchNo(dto.getBatchNo());
        batch.setPurchasePrice(dto.getPurchasePrice());
        batch.setExpiryDate(dto.getExpiryDate());
        applyQuantityCorrection(batch, dto.getQuantity());

        batch.setSupplier(resolveSupplier(dto.getSupplierId()));
        batch.setVariant(resolveVariant(dto.getVariantId()));

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
    public void delete(Long id) {
        batchRepository.delete(findBatch(id));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<ProductBatchDTO> toDTOList(List<ProductBatch> batches) {
        return batches.stream().map(ProductBatchMapper::toDTO).toList();
    }

    private ProductBatch findBatch(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found: " + id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found: " + id));
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null)
            return null;
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found: " + supplierId));
    }

    private ProductVariant resolveVariant(Long variantId) {
        if (variantId == null)
            return null;
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found: " + variantId));
    }
}