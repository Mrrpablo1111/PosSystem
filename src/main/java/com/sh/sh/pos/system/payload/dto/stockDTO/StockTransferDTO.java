package com.sh.sh.pos.system.payload.dto.stockDTO;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.StockTransferStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferDTO {
    private Long id;
    private String transferNo;

    private Long requestId;

    private Long fromBranchId;
    private String fromBranchName;

    private Long toBranchId;
    private String toBranchName;

    private StockTransferStatus status;

    private String carrier;
    private String trackingNo;
    private String vehicleNo;
    private String driverName;
    private String driverPhone;

    private String note;

    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;

    private Long shippedById;
    private String shippedByName;

    private Long receivedById;
    private String receivedByName;

    private List<StockTransferItemDTO> items;
    private List<StockTransferHistoryDTO> histories;
}
