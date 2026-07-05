package com.sh.sh.pos.system.mapper.ordermapper;

import com.sh.sh.pos.system.mapper.orderitemmapper.OrderItemMapper;
import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.order.Order;
import com.sh.sh.pos.system.payload.dto.orderdto.OrderDTO;

public class OrderMapper {
	private OrderMapper() {}
	public static OrderDTO toDTO(Order order) {
		return OrderDTO.builder()
				.id(order.getId())
				.totalAmount(order.getTotalAmount())
				.branchId(order.getBranch().getId())
				.cashier(UserMapper.toDTO(order.getCashier()))
				.customer(order.getCustomer())
				.paymentType(order.getPaymentType())
				.createdAt(order.getCreatedAt())
				.items(order.getItems().stream().map(OrderItemMapper::toDTO)
						.toList())
				.build();
	}

}
 