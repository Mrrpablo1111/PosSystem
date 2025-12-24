package com.sh.sh.pos.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long >{

}
