package com.sh.sh.pos.system.service.serviceimpl.shiftreportserviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.order.Order;
import com.sh.sh.pos.system.model.orderitem.OrderItem;
import com.sh.sh.pos.system.model.paymentsummary.PaymentSummary;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.refund.Refund;
import com.sh.sh.pos.system.model.shiftreport.ShiftReport;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.orderrepository.OrderRepository;
import com.sh.sh.pos.system.repository.refundrepository.RefundRepository;
import com.sh.sh.pos.system.repository.shiftreportrepository.ShiftReportRepository;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.shiftreportservice.ShiftReportService;
import com.sh.sh.pos.system.service.userservice.UserService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftReportServiceImpl implements ShiftReportService {

	private static final String SHIFT_REPORT_NOT_FOUND = "Shift report not found with id: ";

	private final ShiftReportRepository shiftReportRepository;
	private final UserService userService;
	private final RefundRepository refundRepository;
	private final OrderRepository orderRepository;
	private final BranchRepository branchRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public ShiftReport startShift(Long cashierId, Long branchId, LocalDateTime shiftStart) {

		User currentUser = userService.getCurrentUser();
		LocalDateTime actualStart = LocalDateTime.now();

		Branch branch = branchRepository.findById(branchId)
				.orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

		LocalDateTime startOfDay = actualStart.toLocalDate().atStartOfDay();
		LocalDateTime endOfDay = actualStart.toLocalDate().atTime(LocalTime.MAX);

		Optional<ShiftReport> existing = shiftReportRepository.findByCashierAndShiftStartBetween(
				currentUser, startOfDay, endOfDay);

		if (existing.isPresent()) {
			throw new UserException("Shift already started today");
		}

		ShiftReport shiftReport = new ShiftReport();
		shiftReport.setCashier(currentUser);
		shiftReport.setBranch(branch);
		shiftReport.setShiftStart(actualStart);

		return shiftReportRepository.save(shiftReport);
	}

	@Override
	@Transactional
	public ShiftReport endShift(Long shiftReportId, LocalDateTime shiftEnd) {
		User currentUser = userService.getCurrentUser();

		ShiftReport shiftReport = shiftReportRepository
				.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(currentUser)
				.orElseThrow(() -> new ResourceNotFoundException("No active shift found to end"));

		shiftReport.setShiftEnd(shiftEnd);

		List<Refund> refunds = refundRepository.findByCashierAndCreatedAtBetween(
				shiftReport.getCashier(), shiftReport.getShiftStart(), shiftEnd);
		List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(
				shiftReport.getCashier(), shiftReport.getShiftStart(), shiftEnd);

		populateShiftTotals(shiftReport, orders, refunds);
		log.debug("Shift {} ended — totalSales={} totalRefunds={}",
				shiftReport.getId(), shiftReport.getTotalSales(), shiftReport.getTotalRefunds());

		return shiftReportRepository.save(shiftReport);
	}

	@Override
	public ShiftReport getShiftReportById(Long id) {
		return shiftReportRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(SHIFT_REPORT_NOT_FOUND + id));
	}

	@Override
	public List<ShiftReport> getAllShiftReports() {
		return shiftReportRepository.findAll();
	}

	@Override
	public List<ShiftReport> getShiftReportByBranchId(Long branchId) {
		Branch branch = branchRepository.findById(branchId)
				.orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
		return shiftReportRepository.findByBranch(branch);
	}

	@Override
	public List<ShiftReport> getShiftReportByCashierId(Long cashierId) {
		User cashier = userRepository.findById(cashierId)
				.orElseThrow(() -> new ResourceNotFoundException("Cashier not found with id: " + cashierId));
		return shiftReportRepository.findByCashier(cashier);
	}

	@Override
	public ShiftReport getCurrentShiftProgress(Long cashierId) {
		User cashier = userService.getCurrentUser();
		ShiftReport shiftReport = shiftReportRepository
				.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier)
				.orElseThrow(() -> new ResourceNotFoundException("No active shift found for cashier"));

		LocalDateTime now = LocalDateTime.now();

		List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(
				cashier, shiftReport.getShiftStart(), now);
		List<Refund> refunds = refundRepository.findByCashierAndCreatedAtBetween(
				cashier, shiftReport.getShiftStart(), now);

		populateShiftTotals(shiftReport, orders, refunds);

		return shiftReport;
	}

	@Override
	public ShiftReport getShiftByCashierAndDate(Long cashierId, LocalDateTime date) {
		User cashier = userRepository.findById(cashierId)
				.orElseThrow(() -> new ResourceNotFoundException("Cashier not found with id: " + cashierId));

		LocalDateTime start = date.toLocalDate().atStartOfDay();
		LocalDateTime end = date.toLocalDate().atTime(LocalTime.MAX);

		return shiftReportRepository.findByCashierAndShiftStartBetween(cashier, start, end)
				.orElseThrow(() -> new ResourceNotFoundException("No shift report found on this date"));
	}

	@Override
	@Transactional
	public void deleteShiftReport(Long id) {
		if (!shiftReportRepository.existsById(id)) {
			throw new ResourceNotFoundException(SHIFT_REPORT_NOT_FOUND + id);
		}
		shiftReportRepository.deleteById(id);
	}

	// ── Helpers ───────────────────────────────────────────────────────────

	/**
	 * Computes and sets all shift totals (sales, refunds, net, counts, top
	 * products, recent orders, payment summaries) — shared by endShift and
	 * getCurrentShiftProgress.
	 */
	private void populateShiftTotals(ShiftReport shiftReport, List<Order> orders, List<Refund> refunds) {
		BigDecimal totalRefunds = sumAmounts(refunds.stream().map(Refund::getAmount).toList());
		BigDecimal totalSales = sumAmounts(orders.stream().map(Order::getTotalAmount).toList());

		shiftReport.setTotalOrder(orders.size());
		shiftReport.setTotalRefunds(totalRefunds);
		shiftReport.setTotalSales(totalSales);
		shiftReport.setNetSale(totalSales.subtract(totalRefunds));
		shiftReport.setRecentOrders(getRecentOrders(orders));
		shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
		shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
		shiftReport.setRefunds(refunds);
	}

	private BigDecimal sumAmounts(List<BigDecimal> amounts) {
		return amounts.stream()
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private List<PaymentSummary> getPaymentSummaries(List<Order> orders, BigDecimal totalSales) {

		Map<PaymentType, List<Order>> grouped = orders.stream()
				.collect(Collectors.groupingBy(
						order -> order.getPaymentType() != null ? order.getPaymentType() : PaymentType.CASH));

		List<PaymentSummary> summaries = new ArrayList<>();

		for (Map.Entry<PaymentType, List<Order>> entry : grouped.entrySet()) {
			BigDecimal amount = sumAmounts(
					entry.getValue().stream().map(Order::getTotalAmount).toList());

			int transactions = entry.getValue().size();
			BigDecimal percent = BigDecimal.ZERO;

			if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
				percent = amount
						.multiply(BigDecimal.valueOf(100))
						.divide(totalSales, 2, RoundingMode.HALF_UP);
			}

			PaymentSummary ps = new PaymentSummary();
			ps.setType(entry.getKey());
			ps.setTotalAmount(amount);
			ps.setTransactionCount(transactions);
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
				productSalesMap.merge(product, item.getQuantity(), Integer::sum);
			}
		}

		return productSalesMap.entrySet().stream()
				.sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
				.limit(5)
				.map(Map.Entry::getKey)
				.toList();
	}

	private List<Order> getRecentOrders(List<Order> orders) {
		return orders.stream()
				.sorted(Comparator.comparing(Order::getCreatedAt).reversed())
				.limit(5)
				.toList();
	}
}