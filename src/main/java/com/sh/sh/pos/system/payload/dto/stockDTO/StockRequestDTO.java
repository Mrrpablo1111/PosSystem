package com.sh.sh.pos.system.payload.dto.stockDTO;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.StockRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDTO {
    private Long id;
    private String requestNo;

    private Long fromBranchId;
    private String fromBranchName;

    private Long toBranchId;
    private String toBranchName;

    private StockRequestStatus status;
    private String note;

    private Long requestedById;
    private String requestedByName;

    private Long approvedById;
    private String approvedByName;

    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    private List<StockRequestItemDTO> items;
}
