package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.model.PaymentSummary;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ShiftReportDTO {

	private Long id;
	private LocalDateTime shiftStart;
	private LocalDateTime shiftEnd;
	private Double totalSales;
	private Double totalRefunds;
	private Double netSale;
	private int totalOrder;
	private UserDTO cashier ;
	private Long cashierId;
	private Long branchId;
	private BranchDTO branch;
	private List<PaymentSummary> paymentSummaries;
	private List<ProductDTO> topSellingProducts;
	private List<OrderDTO> recentOrders;
	private List<RefundDTO> refunds;
	

}
