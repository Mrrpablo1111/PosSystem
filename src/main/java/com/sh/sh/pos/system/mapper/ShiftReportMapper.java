package com.sh.sh.pos.system.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.model.ShiftReport;
import com.sh.sh.pos.system.payload.dto.OrderDTO;
import com.sh.sh.pos.system.payload.dto.ProductDTO;
import com.sh.sh.pos.system.payload.dto.RefundDTO;
import com.sh.sh.pos.system.payload.dto.ShiftReportDTO;

public class ShiftReportMapper {
	
	public static ShiftReportDTO toDTO(ShiftReport entity) {
		return ShiftReportDTO.builder()
				.id(entity.getId())
				.shiftEnd(entity.getShiftEnd())
				.shiftStart(entity.getShiftStart())
				.totalSales(entity.getTotalSales())
				.totalOrder(entity.getTotalOrder())
				.totalRefunds(entity.getTotalRefunds())
				.netSale(entity.getNetSale())
				.totalOrder(entity.getTotalOrder())
				.cashier(UserMapper.toDTO(entity.getCashier()))
				.cashierId(entity.getCashier()!=null ? entity.getCashier().getId() : null)
				.branchId(entity.getBranch()!=null ? entity.getBranch().getId() : null)
				.recentOrders(mapOrders(entity.getRecentOrders()))
				.topSellingProducts(mapProducts(entity.getTopSellingProducts()))
				.refunds(mapRefunds(entity.getRefunds()))
				.paymentSummaries(entity.getPaymentSummaries())
				.build();
	}

	private static List<RefundDTO> mapRefunds(List<Refund> refunds) {
		if(refunds == null || refunds.isEmpty()) {return null;}
		return refunds.stream().map(RefundMapper::toDTO).collect(Collectors.toList());
	}

	private static List<ProductDTO> mapProducts(List<Product> topSellingProducts) {
		if(topSellingProducts == null || topSellingProducts.isEmpty()) {return null;}
		return topSellingProducts.stream().map(ProductMapper::toDTO).collect(Collectors.toList());
	}

	private static List<OrderDTO> mapOrders(List<Order> recentOrders) {
		if(recentOrders == null || recentOrders.isEmpty()) {return null;}
		return recentOrders.stream().map(OrderMapper::toDTO).collect(Collectors.toList());
	}

}
