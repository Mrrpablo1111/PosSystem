package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One data point of the last-N-months receiving trend chart. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReceivingTrendDTO {
    private Integer year;
    private Integer month;         // 1–12
    private Long    receiptCount;
    private Long    unitsReceived;
}
