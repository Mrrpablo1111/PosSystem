package com.sh.sh.pos.system.mapper.goodreceiptmapper;

import java.util.List;

import com.sh.sh.pos.system.model.goodreceipt.GoodsReceipt;
import com.sh.sh.pos.system.model.goodreceipt.GoodsReceiptItem;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptItemDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptResponseDTO;

/**
 * ADAPT: the source paths "supplier.name", "warehouse.name", "branch.name",
 * "receivedBy.fullName", "product.name", "productVariant.name" must match the
 * actual field names on your existing entities. Change them here only —
 * nothing else in the module depends on those names.
 */




import org.springframework.stereotype.Component;


/**
 * Manual mapper (replaces the MapStruct interface). Same class name, package,
 * and method signatures — GoodsReceiptServiceImpl injects and uses it
 * unchanged. No annotation processing involved.
 */
@Component
public class GoodsReceiptMapper {

    // ── Detail ────────────────────────────────────────────────────────────

    public GoodsReceiptDTO toDto(GoodsReceipt e) {
        if (e == null) return null;
        return GoodsReceiptDTO.builder()
                .id(e.getId())
                .grnNumber(e.getGrnNumber())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .purchaseOrderId(e.getPurchaseOrder() != null ? e.getPurchaseOrder().getId() : null)
                .poNumber(e.getPurchaseOrder() != null ? e.getPurchaseOrder().getPoNumber() : null)
                .supplierId(e.getSupplier() != null ? e.getSupplier().getId() : null)
                .supplierName(e.getSupplier() != null ? e.getSupplier().getName() : null)
                .warehouseId(e.getWarehouse() != null ? e.getWarehouse().getId() : null)
                .warehouseName(e.getWarehouse() != null ? e.getWarehouse().getName() : null)
                .branchId(e.getBranch() != null ? e.getBranch().getId() : null)
                // ADAPT: change getName() if your Branch entity uses a different display field
                .branchName(e.getBranch() != null ? e.getBranch().getName() : null)
                .receivedDate(e.getReceivedDate())
                .receivedById(e.getReceivedBy() != null ? e.getReceivedBy().getId() : null)
                .receivedByName(e.getReceivedBy() != null ? e.getReceivedBy().getFullName() : null)
                .note(e.getNote())
                .totalReceivedQuantity(sum(e, Field.RECEIVED))
                .totalAcceptedQuantity(sum(e, Field.ACCEPTED))
                .totalDamagedQuantity(sum(e, Field.DAMAGED))
                .totalRejectedQuantity(sum(e, Field.REJECTED))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .items(toItemDtoList(e.getItems()))
                .build();
    }

    // ── List row ──────────────────────────────────────────────────────────

    public GoodsReceiptResponseDTO toResponseDto(GoodsReceipt e) {
        if (e == null) return null;
        return GoodsReceiptResponseDTO.builder()
                .id(e.getId())
                .grnNumber(e.getGrnNumber())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .purchaseOrderId(e.getPurchaseOrder() != null ? e.getPurchaseOrder().getId() : null)
                .poNumber(e.getPurchaseOrder() != null ? e.getPurchaseOrder().getPoNumber() : null)
                .supplierName(e.getSupplier() != null ? e.getSupplier().getName() : null)
                .warehouseName(e.getWarehouse() != null ? e.getWarehouse().getName() : null)
                .receivedDate(e.getReceivedDate())
                .receivedByName(e.getReceivedBy() != null ? e.getReceivedBy().getFullName() : null)
                .itemCount(e.getItems() != null ? e.getItems().size() : 0)
                .totalReceivedQuantity(sum(e, Field.RECEIVED))
                .totalDamagedQuantity(sum(e, Field.DAMAGED))
                .createdAt(e.getCreatedAt())
                .build();
    }

    public List<GoodsReceiptResponseDTO> toResponseDtoList(List<GoodsReceipt> entities) {
        return entities == null ? List.of()
                : entities.stream().map(this::toResponseDto).toList();
    }

    // ── Item ──────────────────────────────────────────────────────────────

    public GoodsReceiptItemDTO toItemDto(GoodsReceiptItem i) {
        if (i == null) return null;
        return GoodsReceiptItemDTO.builder()
                .id(i.getId())
                .purchaseOrderItemId(i.getPurchaseOrderItem() != null ? i.getPurchaseOrderItem().getId() : null)
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProduct() != null ? i.getProduct().getName() : null)
                .variantId(i.getProductVariant() != null ? i.getProductVariant().getId() : null)
                .variantName(i.getProductVariant() != null ? i.getProductVariant().getName() : null)
                .requestedQuantity(i.getRequestedQuantity())
                .receivedQuantity(i.getReceivedQuantity())
                .acceptedQuantity(i.getAcceptedQuantity())
                .damagedQuantity(i.getDamagedQuantity())
                .rejectedQuantity(i.getRejectedQuantity())
                .unitCost(i.getUnitCost())
                .batchNumber(i.getBatchNumber())
                .expiryDate(i.getExpiryDate())
                .manufactureDate(i.getManufactureDate())
                .note(i.getNote())
                .build();
    }

    public List<GoodsReceiptItemDTO> toItemDtoList(List<GoodsReceiptItem> items) {
        return items == null ? List.of()
                : items.stream().map(this::toItemDto).toList();
    }

    // ── Aggregates ────────────────────────────────────────────────────────

    private enum Field { RECEIVED, ACCEPTED, DAMAGED, REJECTED }

    private Integer sum(GoodsReceipt e, Field field) {
        if (e.getItems() == null) return 0;
        return e.getItems().stream()
                .mapToInt(i -> nz(switch (field) {
                    case RECEIVED -> i.getReceivedQuantity();
                    case ACCEPTED -> i.getAcceptedQuantity();
                    case DAMAGED -> i.getDamagedQuantity();
                    case REJECTED -> i.getRejectedQuantity();
                }))
                .sum();
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}