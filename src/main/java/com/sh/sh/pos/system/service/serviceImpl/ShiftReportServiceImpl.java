package com.sh.sh.pos.system.service.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.ShiftReportMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.OrderItem;
import com.sh.sh.pos.system.model.PaymentSummary;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.model.ShiftReport;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.payload.dto.ShiftReportDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.OrderRepository;
import com.sh.sh.pos.system.repository.RefundRepository;
import com.sh.sh.pos.system.repository.ShiftReportRepository;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.ShiftReportService;
import com.sh.sh.pos.system.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftReportServiceImpl implements ShiftReportService {

	private final ShiftReportRepository shiftReportRepository;
	private final UserService userService;
	private final RefundRepository refundRepository;
	private final OrderRepository orderRepository;
	private final BranchRepository branchRepository;
	private final UserRepository userRepository;

	@Override
	public ShiftReport startShift(Long cashierId, Long branchId, LocalDateTime shiftStart) throws UserException {

		User currentUser = userService.getCurrentUser();
		shiftStart = LocalDateTime.now();

		Branch branch = branchRepository.findById(branchId)
				.orElseThrow(() -> new RuntimeException("Branch not found with ID: " + branchId));
		LocalDateTime startOfDay = shiftStart.withHour(0).withMinute(0).withSecond(0);
		LocalDateTime endOfDay = shiftStart.withHour(23).withMinute(59).withSecond(59);

		Optional<ShiftReport> existing = shiftReportRepository.findByCashierAndShiftStartBetween(currentUser,
				startOfDay, endOfDay);

		if (existing.isPresent()) {
			throw new RuntimeException("Shift already started today");

		}

		ShiftReport shiftReport = new ShiftReport();
		shiftReport.setCashier(currentUser);
		shiftReport.setBranch(branch);
		shiftReport.setShiftStart(shiftStart);

		return shiftReportRepository.save(shiftReport);
	}

	@Override
	@Transactional
	public ShiftReport endShift(Long shiftReportId, LocalDateTime shiftEnd) throws UserException {
		User currentUser = userService.getCurrentUser();

		ShiftReport shiftReport = shiftReportRepository
				.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(currentUser)
				.orElseThrow(() -> new EntityNotFoundException("Shift not found"));

		shiftReport.setShiftEnd(shiftEnd);
		List<Refund> refunds = refundRepository.findByCashierAndCreatedAtBetween(
				shiftReport.getCashier(), shiftReport.getShiftStart(), shiftEnd);
		BigDecimal totalRefunds = refunds.stream()
				.map(Refund::getAmount)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		System.out.println("total Refund: " + totalRefunds);

		List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(
				shiftReport.getCashier(), shiftReport.getShiftStart(), shiftEnd);
		BigDecimal totalSales = orders.stream()
				.map(Order::getTotalAmount)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		int totalOrders = orders.size();

		BigDecimal netSales = totalSales.subtract(totalRefunds);

		shiftReport.setTotalOrder(totalOrders);
		shiftReport.setTotalRefunds(totalRefunds);
		shiftReport.setTotalSales(totalSales);
		shiftReport.setNetSale(netSales);
		shiftReport.setRecentOrders(getRecentOrders(orders));
		shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
		shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
		shiftReport.setRefunds(refunds);

		return shiftReportRepository.save(shiftReport);
	}

	@Override
	public ShiftReport getShiftReportById(Long id) {

		return shiftReportRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift report not found"));
	}

	@Override
	public List<ShiftReport> getAllShiftReports() {
		return shiftReportRepository.findAll();
	}

	@Override
	public List<ShiftReport> getShiftReportByBranchId(Long branchId) {
		Branch branch = branchRepository.findById(branchId)
				.orElseThrow(() -> new RuntimeException("Branch not found"));
		return shiftReportRepository.findByBranch(branch);

	}

	@Override
	public List<ShiftReport> getShiftReportByCashierId(Long cashierId) {
		User cashier = userRepository.findById(cashierId)
				.orElseThrow(() -> new RuntimeException("Cashier not found"));
		return shiftReportRepository.findByCashier(cashier);
	}

	@Override
	public ShiftReport getCurrentShiftProgress(Long cashierId) throws UserException {
		User cashier = userService.getCurrentUser();
		ShiftReport shiftReport = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier)
				.orElseThrow(
						() -> new RuntimeException("no active shift found for cashier"));

		LocalDateTime now = LocalDateTime.now();

		List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(cashier, shiftReport.getShiftStart(),
				now);

		List<Refund> refunds = refundRepository.findByCashierAndCreatedAtBetween(
				cashier, shiftReport.getShiftStart(), now);

		BigDecimal totalRefunds = refunds.stream()
				.map(Refund::getAmount)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalSales = orders.stream()
				.map(Order::getTotalAmount)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		int totalOrders = orders.size();

		BigDecimal netSales = totalSales.subtract(totalRefunds);

		shiftReport.setTotalOrder(totalOrders);
		shiftReport.setTotalRefunds(totalRefunds);
		shiftReport.setTotalSales(totalSales);
		shiftReport.setNetSale(netSales);
		shiftReport.setRecentOrders(getRecentOrders(orders));
		shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
		shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
		shiftReport.setRefunds(refunds);

		return shiftReport;
	}

	@Override
	public ShiftReport getShiftByCashierAndDate(Long cashierId, LocalDateTime date) {
		User cashier = userRepository.findById(cashierId)
				.orElseThrow(() -> new RuntimeException("cashier not found with given id" + cashierId));

		LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0);

		LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59);

		return shiftReportRepository.findByCashierAndShiftStartBetween(cashier, start, end)
				.orElseThrow(() -> new RuntimeException("No shift report found on this date"));
	}

	// ------------------Helper Method s---------------------

	private List<PaymentSummary> getPaymentSummaries(List<Order> orders, BigDecimal totalSales) {

		Map<PaymentType, List<Order>> grouped = orders.stream()
				.collect(Collectors.groupingBy(
						order -> order.getPaymentType() != null ? order.getPaymentType() : PaymentType.CASH));

		List<PaymentSummary> summaries = new ArrayList<>();

		for (Map.Entry<PaymentType, List<Order>> entry : grouped.entrySet()) {
			BigDecimal amount = entry.getValue()
					.stream()
					.map(Order::getTotalAmount)
					.filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			int tansactions = entry.getValue().size();
			BigDecimal percent = BigDecimal.ZERO;

			if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
				percent = amount
						.multiply(BigDecimal.valueOf(100))
						.divide(totalSales, 2, RoundingMode.HALF_UP);
			}
			PaymentSummary ps = new PaymentSummary();
			ps.setType(entry.getKey());
			ps.setTotalAmount(amount);
			ps.setTransactionCount(tansactions);
			ps.setPercentage(percent);
			summaries.add(ps);
		}
		return summaries;
	}

	private List<Product> getTopSellingProducts(List<Order> orders) {
		Map<Product, Integer> productSalesMap = new HashMap<>();

		for (Order order : orders) {
			for (OrderItem item : order.getItems()) {

				Product product = item.getProduct();

				productSalesMap.put(product, productSalesMap.getOrDefault(product, 0) + item.getQuantity());

			}
		}
		return productSalesMap.entrySet().stream()
				.sorted((a, b) -> b.getValue()
						.compareTo(a.getValue()))
				.limit(5)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	private List<Order> getRecentOrders(List<Order> orders) {

		return orders.stream().sorted(Comparator.comparing(Order::getCreatedAt).reversed()).limit(5)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteShiftReport(Long id) {
		if (!shiftReportRepository.existsById(id)) {
			throw new RuntimeException("Shift report not found");
		}
		shiftReportRepository.deleteById(id);
	}

}
