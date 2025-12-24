package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.PaymentType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Builder
@Table(name = "orders")
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private Double totalAmount;
	
	private LocalDateTime createdAt;
	
	@ManyToOne
	private Branch branch;
	
	@ManyToOne
	private User cashier;
	
	@ManyToOne
	private Customer customer;
	
	private PaymentType paymentType;
	
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> items;
	
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		
	} 
}
