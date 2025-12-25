package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.OrderStatus;
import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.mapper.OrderMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.OrderItem;
import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.OrderDTO;
import com.sh.sh.pos.system.repository.OrderItemRepository;
import com.sh.sh.pos.system.repository.OrderRepository;
import com.sh.sh.pos.system.repository.ProductRepository;
import com.sh.sh.pos.system.service.OrderService;
import com.sh.sh.pos.system.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

	private final UserService userService;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;

	
	
	@Override
	public OrderDTO createOrder(OrderDTO orderDTO) throws Exception {
		User cashier = userService.getCurrentUser();
		
		Branch branch = cashier.getBranch();
		if(branch == null) {
			throw new Exception("cashier's branch not found");
		}
		
		Order order = Order.builder()
				.branch(branch)
				.cashier(cashier)
				.customer(orderDTO.getCustomer())
				.paymentType(orderDTO.getPaymentType())
				.build(); 
		orderRepository.save(order); 
		
		List<OrderItem> orderItems = orderDTO
				.getItems()
				.stream()
				.map(itemDTO -> {
					Product product =  productRepository.findById(itemDTO.getProductId()).orElseThrow(
							()-> new EntityNotFoundException ("product not found"));
					
//					return orderItemRepository.save(orderItem); 
					
					return OrderItem.builder()
							.product(product)
							.quantity(itemDTO.getQuantity())
							.price(product.getSellingPrice() * itemDTO.getQuantity())
							.order(order)
							.build();
			
		}).collect(Collectors.toList());
		double total = orderItems.stream().mapToDouble(
				OrderItem::getPrice).sum();
		order.setTotalAmount(total);
		order.setItems(orderItems);
		
		Order savedOrder = orderRepository.save(order);
		
		return OrderMapper.toDTO(savedOrder);
	}

	@Override
	public OrderDTO getOrderById(Long id) throws Exception {
		
		return orderRepository.findById(id)
				.map(OrderMapper::toDTO)
				.orElseThrow(
						() -> new Exception("order not found with id" + id)
						)
				;
	}

	@Override
	public List<OrderDTO> getOrdersByBranch(Long branchId, Long customerId, Long cashierId, PaymentType paymentType,
			OrderStatus status) {
		
		return orderRepository.findByBranchId(branchId).stream()
				.filter(order -> customerId == null
				|| (order.getCustomer()!=null &&
					order.getCustomer().getId().equals(customerId)))
				.filter(order -> cashierId == null 
				|| (order.getCashier()!=null &&
					order.getCashier().getId().equals(cashierId)))
				.filter(order -> paymentType==null ||
						order.getPaymentType()==paymentType)
				.map(OrderMapper::toDTO).collect(Collectors.toList())
				;
	}

	@Override
	public List<OrderDTO> getOrdersByCashier(Long cashierId) {
		
		return orderRepository.findByCashierId(cashierId).stream().map(OrderMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public void deleteOrder(Long id) throws Exception {
		Order order = orderRepository.findById(id).orElseThrow(()-> new Exception("order not found with id" + id));
		orderRepository.delete(order); 
		
	}

	@Override
	public List<OrderDTO> getTodayOrdersByBranch(Long branchId) {
		LocalDate today = LocalDate.now();
		LocalDateTime start = today.atStartOfDay();
		
		LocalDateTime end = today.plusDays(1).atStartOfDay();
		
		return orderRepository.findByBranchIdAndCreatedAtBetween(branchId, start, end).stream().map(OrderMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
		
		return orderRepository.findByCustomerId(customerId).stream().map(OrderMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId) {
		
		return orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId)
				.stream().map(OrderMapper::toDTO).collect(Collectors.toList());
				
	}

}
