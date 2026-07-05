package com.sh.sh.pos.system.mapper.shiftreportmapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.mapper.ordermapper.OrderMapper;
import com.sh.sh.pos.system.mapper.productmapper.ProductMapper;
import com.sh.sh.pos.system.mapper.refundmapper.RefundMapper;
import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.order.Order;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.refund.Refund;
import com.sh.sh.pos.system.model.shiftreport.ShiftReport;
import com.sh.sh.pos.system.payload.dto.orderdto.OrderDTO;
import com.sh.sh.pos.system.payload.dto.productdto.ProductDTO;
import com.sh.sh.pos.system.payload.dto.refunddto.RefundDTO;
import com.sh.sh.pos.system.payload.dto.shiftreportdto.ShiftReportDTO;

@Component
public class ShiftReportMapper {
	private ShiftReportMapper() {
	}

	public static ShiftReportDTO toDTO(ShiftReport shiftReport) {
		if (shiftReport == null) {
			return null;
		}
		ShiftReportDTO dto = new ShiftReportDTO();
		dto.setId(shiftReport.getId());
		dto.setShiftStart(shiftReport.getShiftStart());
		dto.setShiftEnd(shiftReport.getShiftEnd());
		dto.setTotalSales(shiftReport.getTotalSales() != null ? shiftReport.getTotalSales() : BigDecimal.ZERO);
		dto.setTotalOrder(shiftReport.getTotalOrder());
		dto.setTotalRefunds(shiftReport.getTotalRefunds() != null ? shiftReport.getTotalRefunds() : BigDecimal.ZERO);
		dto.setNetSale(shiftReport.getNetSale() != null ? shiftReport.getNetSale() : BigDecimal.ZERO);
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
		if (refunds == null) {
			return Collections.emptyList();
		}
		return refunds.stream().map(RefundMapper::toDTO).toList();
	}

	private static List<ProductDTO> mapProducts(List<Product> products) {
		if (products == null) {
			return Collections.emptyList();
		}
		return products.stream().map(ProductMapper::toDTO).toList();
	}

	private static List<OrderDTO> mapOrders(List<Order> orders) {
		if (orders == null) {
			return Collections.emptyList();
		}
		return orders.stream().map(OrderMapper::toDTO).toList();
	}
}
