package com.sh.sh.pos.system.mapper.inventorytransactionmapper;



import java.util.List;

import com.sh.sh.pos.system.model.inventorytransaction.InventoryTransaction;
import com.sh.sh.pos.system.payload.dto.inventorytransactiondto.InventoryTransactionDTO;

public class InventoryTransactionMapper {

    private InventoryTransactionMapper() {}

    public static InventoryTransactionDTO toDTO(InventoryTransaction t) {
        if (t == null) return null;

        InventoryTransactionDTO dto = new InventoryTransactionDTO();
        dto.setId(t.getId());

        if (t.getProduct() != null) {
            dto.setProductId(t.getProduct().getId());
            dto.setProductName(t.getProduct().getName());
        }
        if (t.getProductVariant() != null) {
            dto.setVariantId(t.getProductVariant().getId());
            dto.setVariantName(t.getProductVariant().getName());
        }
        if (t.getWarehouse() != null) {
            dto.setWarehouseId(t.getWarehouse().getId());
            dto.setWarehouseName(t.getWarehouse().getName());
        }

        dto.setQuantity(t.getQuantity());
        dto.setUnitCost(t.getUnitCost());
        dto.setDirection(t.getQuantity() != null && t.getQuantity() < 0 ? "OUT" : "IN");

        dto.setReferenceType(t.getReferenceType());
        dto.setReferenceId(t.getReferenceId());

        // ADAPT: confirm User's display-name getter — assumed getName() below;
        // change to e.g. getFullName() / getUsername() to match your User entity.
        dto.setPerformedByName(t.getPerformedBy() != null ? t.getPerformedBy().getFullName() : null);

        dto.setNote(t.getNote());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    public static List<InventoryTransactionDTO> toDTOList(List<InventoryTransaction> transactions) {
        return transactions == null ? List.of()
                : transactions.stream().map(InventoryTransactionMapper::toDTO).toList();
    }
}

