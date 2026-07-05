package com.sh.sh.pos.system.repository.orderitemrepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.orderitem.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long >{

}
