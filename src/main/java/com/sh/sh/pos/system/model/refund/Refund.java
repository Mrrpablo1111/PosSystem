package com.sh.sh.pos.system.model.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sh.sh.pos.system.domain.PaymentType;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.order.Order;
import com.sh.sh.pos.system.model.shiftreport.ShiftReport;
import com.sh.sh.pos.system.model.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Refund {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private Order order;
	
	private String reason;
	
	private BigDecimal amount;
	
	@ManyToOne
	@JsonIgnore
	private ShiftReport shiftReport;
	
	@ManyToOne
	private User cashier;
	
	@ManyToOne
	private Branch branch;
	
	private PaymentType paymentType;
	
	private LocalDateTime createdAt;
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		
	}
	
	
	
	

}
