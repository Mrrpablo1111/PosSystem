package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductBatchDTO;

public class ProductBatchMapper {
        public static ProductBatchDTO toDTO(ProductBatch batch) {

                return ProductBatchDTO.builder()
                                .id(batch.getId())
                                .branchId(batch.getBranch().getId())
                                .productId(batch.getProduct().getId())
                                .productName(
                                        batch.getProduct() != null
                                                ? batch.getProduct().getName()
                                                : null)

                                .batchNo(batch.getBatchNo())
                                .purchasePrice(batch.getPurchasePrice())
                                .supplierName(
                                                batch.getSupplier() != null
                                                                ? batch.getSupplier().getName()
                                                                : null)
                                .supplierId(
                                                batch.getSupplier() != null
                                                                ? batch.getSupplier().getId()
                                                                : null)
                                .quantity(batch.getQuantity())
                                .remainingQuantity(batch.getRemainingQuantity())
                                .expiryDate(batch.getExpiryDate())
                                .createdAt(batch.getCreatedAt())
                                .variantId(
                                                batch.getVariant() != null
                                                                ? batch.getVariant().getId()
                                                                : null)

                                .variantName(
                                                batch.getVariant() != null
                                                                ? batch.getVariant().getName()
                                                                : null)
                                .daysLeft(batch.getDaysLeft())
                                .expiryStatus(batch.getExpiryStatus())
                                .build();
        }

        public static ProductBatch toEntity(
                        ProductBatchDTO dto,
                        Product product,
                        Branch branch,
                        Supplier supplier, ProductVariant variant) {

                ProductBatch batch = new ProductBatch();

                batch.setProduct(product);
                batch.setBranch(branch);
                batch.setSupplier(supplier);
                batch.setVariant(variant);
                batch.setBatchNo(dto.getBatchNo());
                batch.setPurchasePrice(dto.getPurchasePrice());
                batch.setQuantity(dto.getQuantity());
                batch.setRemainingQuantity(dto.getQuantity());
                batch.setExpiryDate(dto.getExpiryDate());
                return batch;
        }

}
