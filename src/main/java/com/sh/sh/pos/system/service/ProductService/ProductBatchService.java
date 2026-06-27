package com.sh.sh.pos.system.service.ProductService;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.productDTO.ProductBatchDTO;

public interface ProductBatchService {

    ProductBatchDTO create(ProductBatchDTO dto);

    ProductBatchDTO update(Long id, ProductBatchDTO dto);

    void delete(Long id);

    ProductBatchDTO get(Long id);

    List<ProductBatchDTO> getByProduct(Long productId);

    List<ProductBatchDTO> getByBranch(Long branchId);

    List<ProductBatchDTO> getBySupplier(Long supplierId);

    List<ProductBatchDTO> searchByBatchNo(String batchNo);

    /** Batches that have already passed their expiry date. */
    List<ProductBatchDTO> getExpiredBatches();

    /** Batches expiring within the next {@code days} days (not yet expired). */
    List<ProductBatchDTO> getExpiringBatches(int days);
}