package com.sh.sh.pos.system.mapper.stockMapper;

import java.util.List;

import com.sh.sh.pos.system.model.stocks.StockTransfer;
import com.sh.sh.pos.system.model.stocks.StockTransferHistory;
import com.sh.sh.pos.system.model.stocks.StockTransferItem;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockTransferDTO;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockTransferHistoryDTO;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockTransferItemDTO;

public class StockTransferMapper {
     public static StockTransferDTO toDTO(StockTransfer transfer) {
        return StockTransferDTO.builder()
                .id(transfer.getId())
                .transferNo(transfer.getTransferNo())
                .requestId(transfer.getRequest() != null ? transfer.getRequest().getId() : null)
                .fromBranchId(transfer.getFromBranch() != null ? transfer.getFromBranch().getId() : null)
                .fromBranchName(transfer.getFromBranch() != null ? transfer.getFromBranch().getName() : null)
                .toBranchId(transfer.getToBranch() != null ? transfer.getToBranch().getId() : null)
                .toBranchName(transfer.getToBranch() != null ? transfer.getToBranch().getName() : null)
                .status(transfer.getStatus())
                .carrier(transfer.getCarrier())
                .trackingNo(transfer.getTrackingNo())
                .vehicleNo(transfer.getVehicleNo())
                .driverName(transfer.getDriverName())
                .driverPhone(transfer.getDriverPhone())
                .note(transfer.getNote())
                .shippedAt(transfer.getShippedAt())
                .receivedAt(transfer.getReceivedAt())
                .createdAt(transfer.getCreatedAt())
                .shippedById(transfer.getShippedBy() != null ? transfer.getShippedBy().getId() : null)
                .shippedByName(transfer.getShippedBy() != null ? transfer.getShippedBy().getFullName() : null)
                .receivedById(transfer.getReceivedBy() != null ? transfer.getReceivedBy().getId() : null)
                .receivedByName(transfer.getReceivedBy() != null ? transfer.getReceivedBy().getFullName() : null)
                .items(toItemDTOs(transfer.getItems()))
                .histories(toHistoryDTOs(transfer.getHistories()))
                .build();
    }

    public static StockTransferItemDTO toItemDTO(StockTransferItem item) {
        return StockTransferItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                .batchId(item.getBatch() != null ? item.getBatch().getId() : null)
                .batchNo(item.getBatch() != null ? item.getBatch().getBatchNo() : null)
                .requestedQty(item.getRequestedQty())
                .shippedQty(item.getShippedQty())
                .receivedQty(item.getReceivedQty())
                .damagedQty(item.getDamagedQty())
                .missingQty(item.getMissingQty())
                .build();
    }

    public static StockTransferHistoryDTO toHistoryDTO(StockTransferHistory history) {
        return StockTransferHistoryDTO.builder()
                .id(history.getId())
                .transferId(history.getTransfer() != null ? history.getTransfer().getId() : null)
                .status(history.getStatus())
                .remarks(history.getRemarks())
                .changedById(history.getChangedBy() != null ? history.getChangedBy().getId() : null)
                .changedByName(history.getChangedBy() != null ? history.getChangedBy().getFullName() : null)
                .createdAt(history.getCreatedAt())
                .build();
    }

    private static List<StockTransferItemDTO> toItemDTOs(List<StockTransferItem> items) {
        if (items == null) return List.of();
        return items.stream().map(StockTransferMapper::toItemDTO).toList();
    }

    private static List<StockTransferHistoryDTO> toHistoryDTOs(List<StockTransferHistory> histories) {
        if (histories == null) return List.of();
        return histories.stream().map(StockTransferMapper::toHistoryDTO).toList();
    }
}
