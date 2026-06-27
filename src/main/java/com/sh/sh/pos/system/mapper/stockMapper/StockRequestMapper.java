package com.sh.sh.pos.system.mapper.stockMapper;

import java.util.List;

import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.stocks.StockRequest;
import com.sh.sh.pos.system.model.stocks.StockRequestItem;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockRequestDTO;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockRequestItemDTO;

public class StockRequestMapper {
    public static StockRequestDTO toDTO(StockRequest request) {
        return StockRequestDTO.builder()
                .id(request.getId())
                .requestNo(request.getRequestNo())
                .fromBranchId(request.getFromBranch() != null ? request.getFromBranch().getId() : null)
                .fromBranchName(request.getFromBranch() != null ? request.getFromBranch().getName() : null)
                .toBranchId(request.getToBranch() != null ? request.getToBranch().getId() : null)
                .toBranchName(request.getToBranch() != null ? request.getToBranch().getName() : null)
                .status(request.getStatus())
                .note(request.getNote())
                .requestedById(request.getRequestedBy() != null ? request.getRequestedBy().getId() : null)
                .requestedByName(request.getRequestedBy() != null ? request.getRequestedBy().getFullName() : null)
                .approvedById(request.getApprovedBy() != null ? request.getApprovedBy().getId() : null)
                .approvedByName(request.getApprovedBy() != null ? request.getApprovedBy().getFullName() : null)
                .approvedAt(request.getApprovedAt())
                .createdAt(request.getCreatedAt())
                .items(toItemDTOs(request.getItems()))
                .build();
    }

    public static StockRequestItemDTO toItemDTO(StockRequestItem item) {
        return StockRequestItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                .quantity(item.getQuantity())
                .build();
    }

    private static List<StockRequestItemDTO> toItemDTOs(List<StockRequestItem> items) {
        if (items == null) return List.of();
        return items.stream().map(StockRequestMapper::toItemDTO).toList();
    }

    public static StockRequest toEntity(StockRequestDTO dto, Branch fromBranch, Branch toBranch) {
        return StockRequest.builder()
                .fromBranch(fromBranch)
                .toBranch(toBranch)
                .note(dto.getNote())
                .build();
    }
    
}
