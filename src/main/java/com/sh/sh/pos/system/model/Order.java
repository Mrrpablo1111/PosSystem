package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sh.sh.pos.system.domain.OrderStatus;
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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor 
@Entity
@Builder
@Table(name = "orders")
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Double totalAmount;
	
	private LocalDateTime createdAt;
	
	@ManyToOne
	 @JsonIgnore
	private Branch branch;
	
	@ManyToOne
	@JsonIgnore
	private User cashier;
	
	@ManyToOne
	private Customer customer; 
	
	private PaymentType paymentType;
	
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> items;
	
	private OrderStatus status = OrderStatus.COMPLETED;
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		
	} 
}
