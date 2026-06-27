package com.sh.sh.pos.system.model;

import java.math.BigDecimal;

import com.sh.sh.pos.system.model.products.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name = "order_items")
@AllArgsConstructor
@NoArgsConstructor 
@Entity
@Builder
public class OrderItem {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Integer quantity;
	
	private BigDecimal price;
	
	@ManyToOne
	private Product product;
	
	@ManyToOne
	private Order order; 
}
