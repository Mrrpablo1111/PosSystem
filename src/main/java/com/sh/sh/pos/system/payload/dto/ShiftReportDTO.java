package com.sh.sh.pos.system.payload.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.model.PaymentSummary;
import com.sh.sh.pos.system.payload.dto.productDTO.ProductDTO;

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
	private BigDecimal totalSales;
	private BigDecimal totalRefunds;
	private BigDecimal netSale;
	private int totalOrder;
	private UserDTO cashier;
	private Long cashierId;
	private Long branchId;
	private BranchDTO branch;
	private BigDecimal openingCash;

	private BigDecimal closingCash;

	private BigDecimal expectedCash;

	private BigDecimal cashDifference;
	private List<PaymentSummary> paymentSummaries;
	private List<ProductDTO> topSellingProducts;
	private List<OrderDTO> recentOrders;
	private List<RefundDTO> refunds;

}
