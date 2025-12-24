package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.domain.OrderStatus;
import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.payload.dto.OrderDTO;

public interface OrderService {
	OrderDTO createOrder(OrderDTO orderDTO) throws Exception;
	OrderDTO getOrderById(Long id) throws Exception ;
	
	List<OrderDTO> getOrdersByBranch(Long branchId, Long customerId, Long cashierId, PaymentType paymentType, OrderStatus status) throws Exception;
	
	List<OrderDTO> getOrdersByCashier(Long cashierId);
	void deleteOrder(Long id) throws Exception;
	
	List<OrderDTO> getTodayOrdersByBranch(Long branchId) throws Exception;
	List<OrderDTO> getOrdersByCustomerId(Long customerId) throws Exception;
	List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId) throws Exception;
	
}
