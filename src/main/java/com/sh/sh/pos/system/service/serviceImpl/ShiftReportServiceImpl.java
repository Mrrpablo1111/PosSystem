package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.mapper.ShiftReportMapper;
import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.OrderItem;
import com.sh.sh.pos.system.model.PaymentSummary;
import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.model.ShiftReport;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.ShiftReportDTO;
import com.sh.sh.pos.system.repository.OrderRepository;
import com.sh.sh.pos.system.repository.RefundRepository;
import com.sh.sh.pos.system.repository.ShiftReportRepository;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.ShiftReportService;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftReportServiceImpl implements ShiftReportService{
	
	private final ShiftReportRepository shiftReportRepository;
	private final UserService userService;
	private final RefundRepository refundRepository;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	

	@Override
	public ShiftReportDTO startShift() throws Exception {
		
		User currentUser  = userService.getCurrentUser();
		
		LocalDateTime shiftStart = LocalDateTime.now();
		
		LocalDateTime startOfDay = shiftStart.withHour(0).withMinute(0).withSecond(0);
		LocalDateTime endOfDay = shiftStart.withHour(23).withMinute(59).withSecond(59);
		
		Optional<ShiftReport> existing = shiftReportRepository.findByCashierAndShiftStartBetween(currentUser, startOfDay, endOfDay);
		
		if(existing.isPresent()) {
			throw new Exception("Shift already started today"); 
			
		} 
		ShiftReport shiftReport = ShiftReport.builder()
				.cashier(currentUser)
				.shiftStart(shiftStart)
				.build(); 
		
		ShiftReport savedReport = shiftReportRepository.save(shiftReport);
		return ShiftReportMapper.toDTO(savedReport);
	}

	@Override
	public ShiftReportDTO endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception {
		User currentUser = userService.getCurrentUser();
		
		ShiftReport shiftReport = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(currentUser)
				.orElseThrow(()-> new Exception("Shift not found"));
		
		shiftReport.setShiftEnd(shiftEnd);
		 List<Refund> refunds = refundRepository.findByCashierIdAndCreatedAtBetween(
				 currentUser.getId(), 
				 shiftReport.getShiftStart(),
				 shiftReport.getShiftEnd());
		 
		 double totalRefunds = refunds.stream().mapToDouble(refund -> refund.getAmount()!=null?refund.getAmount():0.0).sum(); 
		 
		 List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(currentUser, shiftReport.getShiftStart(), shiftReport.getShiftEnd());
		 
		 double totalSales = orders.stream().mapToDouble(Order::getTotalAmount).sum();
		 
		 int totalOrders = orders.size();
		 
		 double netSales = totalSales - totalRefunds;
		 
		 shiftReport.setTotalOrder(totalOrders);
		 shiftReport.setTotalRefunds(totalRefunds);
		 shiftReport.setTotalSales(totalSales); 
		 shiftReport.setNetSale(netSales);
		 shiftReport.setRecentOrders(getRecentOrders(orders));
		 shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
		 shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
		 shiftReport.setRefunds(refunds); 
		 
		 ShiftReport savedReport = shiftReportRepository.save(shiftReport);
		 
		return ShiftReportMapper.toDTO(savedReport); 
	}

	

	@Override
	public ShiftReportDTO getShiftReportById(Long id) throws Exception {
		
		return shiftReportRepository.findById(id).map(ShiftReportMapper::toDTO).orElseThrow(()-> new Exception("shift report not found with given id" +id));
	}

	@Override
	public List<ShiftReportDTO> getAllShiftReports() {
		List<ShiftReport> reports = shiftReportRepository.findAll();
		return reports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<ShiftReportDTO> getShiftReportByBranchId(Long branchId) {
		List<ShiftReport> reports = shiftReportRepository.findByBranchId(branchId);
		return reports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<ShiftReportDTO> getShiftReportByCashierId(Long cashierId) {
		List<ShiftReport> reports = shiftReportRepository.findByCashierId(cashierId);
		
		return reports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public ShiftReportDTO getCurrentShiftProgress(Long cashierId) throws Exception {
		User user = userService.getCurrentUser();
		ShiftReport shiftReport = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(user)
				.orElseThrow(
						()-> new Exception("no active shift found for cashier"));
		
		LocalDateTime now = LocalDateTime.now();
		
		List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(user, shiftReport.getShiftStart(), now);
		
		List<Refund> refunds  = refundRepository.findByCashierIdAndCreatedAtBetween(
				 user.getId(), 
				 shiftReport.getShiftStart(),
				 shiftReport.getShiftEnd());
		 
		 double totalRefunds = refunds.stream().mapToDouble(refund -> refund.getAmount()!=null?refund.getAmount():0.0).sum(); 
		 
		   
		 
		 double totalSales = orders.stream().mapToDouble(Order::getTotalAmount).sum();
		 
		 int totalOrders = orders.size();
		 
		 double netSales = totalSales - totalRefunds;
		 
		 shiftReport.setTotalOrder(totalOrders);
		 shiftReport.setTotalRefunds(totalRefunds);
		 shiftReport.setTotalSales(totalSales); 
		 shiftReport.setNetSale(netSales);
		 shiftReport.setRecentOrders(getRecentOrders(orders));
		 shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
		 shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
		 shiftReport.setRefunds(refunds); 
		 
		 ShiftReport savedReport = shiftReportRepository.save(shiftReport);
		return ShiftReportMapper.toDTO(savedReport );
	}

	@Override
	public ShiftReportDTO getShiftByCashierAndDate(Long cashierId, LocalDateTime date) throws Exception {
		User cashier = userRepository.findById(cashierId).orElseThrow(()->new Exception("cashier not found with given id" + cashierId));
		
		LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0);
		
		LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59);
		
		ShiftReport reports = shiftReportRepository.findByCashierAndShiftStartBetween(cashier, start, end)
				.orElseThrow(()-> new Exception("shiftv not found for cashier"));
		
		return ShiftReportMapper.toDTO(reports); 
	}
	
	// ------------------Helper Method s---------------------
	
	private List<PaymentSummary> getPaymentSummaries(List<Order> orders, double totalSales) {
		
		Map<PaymentType, List<Order>> grouped = orders.stream()
				.collect(Collectors.groupingBy(
						order -> order.getPaymentType()!=null?
								order.getPaymentType():PaymentType.CASH));
		
		List<PaymentSummary> summaries = new ArrayList<>();
		
		for(Map.Entry<PaymentType, List<Order>> entry : grouped.entrySet()) {
			double amount = entry.getValue().stream().mapToDouble(Order::getTotalAmount).sum();
			
			int tansactions = entry.getValue().size();
			double percent = (amount/totalSales)*100;
			
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
		Map<Product, Integer> productSalesMap= new HashMap<>();
		
		for(Order order:orders) {
			for(OrderItem item: order.getItems()) {
				
				Product product = item.getProduct();
				
				productSalesMap.put(product, productSalesMap.getOrDefault(product, 0) + item.getQuantity()); 
				
			}
		}
		return productSalesMap.entrySet().stream()
				.sorted((a,b)->b.getValue()
						.compareTo(a.getValue()))
				.limit(5)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	private List<Order> getRecentOrders(List<Order> orders) {

		return orders.stream().sorted(Comparator.comparing(Order::getCreatedAt).reversed()).limit(5).collect(Collectors.toList());
	}

	

}
