package com.sh.sh.pos.system.service.serviceImpl;

import com.sh.sh.pos.system.repository.BranchRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.OrderStatus;
import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.OrderMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.OrderItem;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.model.products.Product;
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
public class OrderServiceImpl implements OrderService {

	private final BranchRepository branchRepository;
	private final UserService userService;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;

	@Override
	public OrderDTO createOrder(OrderDTO orderDTO) throws UserException {
		User cashier = userService.getCurrentUser();

		Branch branch = cashier.getBranch();
		if (branch == null) {
			throw new UserException("cashier's branch not found");
		}

		Order order = Order.builder()
				.branch(branch)
				.cashier(cashier)
				.customer(orderDTO.getCustomer())
				.paymentType(orderDTO.getPaymentType())
				.build();

		List<OrderItem> orderItems = orderDTO
				.getItems()
				.stream()
				.map(itemDTO -> {
					Product product = productRepository.findById(itemDTO.getProductId()).orElseThrow(
							() -> new EntityNotFoundException("product not found"));

					// return orderItemRepository.save(orderItem);

					return OrderItem.builder()
							.product(product)
							.quantity(itemDTO.getQuantity())
							.price(product.getSellingPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())))
							.order(order)
							.build();

				}).toList();
		BigDecimal total = orderItems.stream().map(OrderItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
		order.setTotalAmount(total);
		order.setItems(orderItems);
		return OrderMapper.toDTO(orderRepository.save(order));
	}

	@Override
	public OrderDTO getOrderById(Long id) {

		return orderRepository.findById(id)
				.map(OrderMapper::toDTO)
				.orElseThrow(
						() -> new EntityNotFoundException("order not found with id" + id));
	}

	@Override
	public List<OrderDTO> getOrdersByBranch(Long branchId, Long customerId, Long cashierId, PaymentType paymentType,
			OrderStatus status) {

		return orderRepository.findByBranchId(branchId).stream()
				.filter(order -> customerId == null
						|| (order.getCustomer() != null &&
								order.getCustomer().getId().equals(customerId)))
				.filter(order -> cashierId == null
						|| (order.getCashier() != null &&
								order.getCashier().getId().equals(cashierId)))
				.filter(order -> paymentType == null ||
						order.getPaymentType() == paymentType)

				.map(OrderMapper::toDTO)
				.sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
				.collect(Collectors.toList());
	}

	@Override
	public List<OrderDTO> getOrdersByCashier(Long cashierId) {
		return orderRepository.findByCashierId(cashierId).stream().map(OrderMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public void deleteOrder(Long id) {
		if (!orderRepository.existsById(id)) {
			throw new EntityNotFoundException("Order not found");
		}
		orderRepository.deleteById(id);

	}

	@Override
	public List<OrderDTO> getTodayOrdersByBranch(Long branchId) {
		LocalDate today = LocalDate.now();
		LocalDateTime start = today.atStartOfDay();

		LocalDateTime end = today.plusDays(1).atStartOfDay();

		return orderRepository.findByBranchIdAndCreatedAtBetween(branchId, start, end).stream().map(OrderMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
		List<Order> orders = orderRepository.findByCustomerId(customerId);
		return orders.stream().map(OrderMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId) {
		branchRepository.findById(branchId)
				.orElseThrow(() -> new EntityNotFoundException("Branch not found with ID:" + branchId));

		List<Order> orders = orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId);
		return orders.stream().map(OrderMapper::toDTO).collect(Collectors.toList());

	}

}
