package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.domain.OrderStatus;
import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.OrderDTO;

public interface OrderService {
	OrderDTO createOrder(OrderDTO orderDTO) throws UserException;
	OrderDTO getOrderById(Long id);
	
	List<OrderDTO> getOrdersByBranch(Long branchId, Long customerId, Long cashierId, PaymentType paymentType, OrderStatus status);
	
	List<OrderDTO> getOrdersByCashier(Long cashierId);
	void deleteOrder(Long id);
	
	List<OrderDTO> getTodayOrdersByBranch(Long branchId);
	List<OrderDTO> getOrdersByCustomerId(Long customerId);
	List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId);
	
}
