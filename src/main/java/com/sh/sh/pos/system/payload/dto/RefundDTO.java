package com.sh.sh.pos.system.payload.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RefundDTO {

	private Long id;
	private Long orderId;
	private String reason;
	private BigDecimal amount;
	private Long shiftReportId;
	private String cashierName;
	private Long branchId;  
	private LocalDateTime createdAt;

}
