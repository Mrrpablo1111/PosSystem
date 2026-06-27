package com.sh.sh.pos.system.payload.dto.stockDTO;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.StockTransferStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferHistoryDTO {
    private Long id;
    private Long transferId;
    private StockTransferStatus status;
    private String remarks;
    private Long changedById;
    private String changedByName;
    private LocalDateTime createdAt;
}
