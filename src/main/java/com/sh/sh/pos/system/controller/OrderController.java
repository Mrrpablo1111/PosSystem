package com.sh.sh.pos.system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.domain.OrderStatus;
import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.OrderDTO;
import com.sh.sh.pos.system.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderService orderService;
	
	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_CASHIER')")
	public ResponseEntity<OrderDTO> createOrder(
			@RequestBody OrderDTO order) throws UserException{
		return ResponseEntity.ok(orderService.createOrder(order));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<OrderDTO> getOrderById(
			@PathVariable Long id
			) {
		return ResponseEntity.ok(orderService.getOrderById(id));
	}
	
	@GetMapping("/branch/{branchId}")
	public ResponseEntity<List<OrderDTO>> getOrderByBranch(
			@PathVariable Long branchId,
	 		@RequestParam(required = false) Long customerId,
			@RequestParam(required = false) Long cashierId,
			@RequestParam(required = false) PaymentType paymentType,
			@RequestParam(required = false) OrderStatus orderStatus
			
			) throws Exception{
		return ResponseEntity.ok(orderService.getOrdersByBranch(branchId, customerId, cashierId, paymentType, orderStatus));
	}
	
	@GetMapping("/cashier/{cashierId}")
	public ResponseEntity<List<OrderDTO>> getOrderByCashier(
			@PathVariable Long cashierId
			) throws Exception{
		return ResponseEntity.ok(orderService.getOrdersByCashier(cashierId));
		
	}
	
	@GetMapping("/today/branch/{branchId}")
	public ResponseEntity<List<OrderDTO>> getTodayOrder(@PathVariable Long branchId) throws Exception{
		return ResponseEntity.ok(orderService.getTodayOrdersByBranch(branchId));
		
	}
	
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<List<OrderDTO>> getCustomerOrder(@PathVariable Long customerId) throws Exception{
		return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
	}
	
	@GetMapping("/recent/{branchId}")
	@PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER', 'ROLE_BRANCH_ADMIN')")
	public ResponseEntity<List<OrderDTO>> getRecentOrder(@PathVariable Long branchId){
		List<OrderDTO> recentOrders = orderService.getTop5RecentOrdersByBranchId(branchId);
		return ResponseEntity.ok(recentOrders);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_STORE_MANAGER') or hasAuthority('ROLE_STORE_ADMIN')")
	public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
		orderService.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}
	
}
