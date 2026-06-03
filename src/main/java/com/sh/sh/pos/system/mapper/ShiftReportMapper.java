package com.sh.sh.pos.system.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.model.ShiftReport;
import com.sh.sh.pos.system.payload.dto.OrderDTO;
import com.sh.sh.pos.system.payload.dto.ProductDTO;
import com.sh.sh.pos.system.payload.dto.RefundDTO;
import com.sh.sh.pos.system.payload.dto.ShiftReportDTO;

@Component
public class ShiftReportMapper {
	
	public static ShiftReportDTO toDTO(ShiftReport shiftReport) {
		if(shiftReport == null) return null;
		ShiftReportDTO dto = new ShiftReportDTO();
				dto.setId(shiftReport.getId());
				dto.setShiftEnd(shiftReport.getShiftEnd());
				dto.setShiftStart(shiftReport.getShiftStart());
				dto.setTotalSales(shiftReport.getTotalSales() != null ? shiftReport.getTotalSales() : 0.0);
				dto.setTotalOrder(shiftReport.getTotalOrder());
				dto.setTotalRefunds(shiftReport.getTotalRefunds() != null ? shiftReport.getTotalRefunds(): 0.0);
				dto.setNetSale(shiftReport.getNetSale() != null ? shiftReport.getNetSale() : 0.0);
				dto.setTotalOrder(shiftReport.getTotalOrder());
				dto.setCashier(UserMapper.toDTO(shiftReport.getCashier()));
				dto.setCashierId(shiftReport.getCashier() != null ? shiftReport.getCashier().getId() : null);
				dto.setBranchId(shiftReport.getBranch() != null ? shiftReport.getBranch().getId() : null);
				dto.setRecentOrders(mapOrders(shiftReport.getRecentOrders()));
				dto.setTopSellingProducts(mapProducts(shiftReport.getTopSellingProducts()));
				dto.setRefunds(mapRefunds(shiftReport.getRefunds()));
				dto.setPaymentSummaries(shiftReport.getPaymentSummaries());
				
				return dto;
	}

	private static List<RefundDTO> mapRefunds(List<Refund> refunds) {
		if(refunds == null) return null;
		return refunds.stream().map(RefundMapper::toDTO).collect(Collectors.toList());
	}

	private static List<ProductDTO> mapProducts(List<Product> products) {
		if(products == null ) return null;
					return products.stream().map(ProductMapper::toDTO).collect(Collectors.toList());
	}

	private static List<OrderDTO> mapOrders(List<Order> orders) {
		if(orders == null) return null;
		return orders.stream().map(OrderMapper::toDTO).collect(Collectors.toList());
	}

}
