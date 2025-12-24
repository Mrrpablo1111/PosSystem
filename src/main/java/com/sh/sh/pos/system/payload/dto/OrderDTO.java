package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.model.Customer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class OrderDTO {

	private Long id;
	
	private Double totalAmount;
	
	private LocalDateTime createdAt;
	

	private Long branchId;
	private Long customerId;
	

	private UserDTO cashier;
	
	private PaymentType paymentType;
	
	private Customer customer;
	

	private List<OrderItemDTO > items;
}
