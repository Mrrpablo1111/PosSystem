package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.PaymentType;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class RefundDTO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	private OrderDTO order;
	private Long orderId;
	
	private String reason;
	
	private Double amount;
	

	
//	private ShiftReport shiftReport;
	private Long shiftReportId;

	private UserDTO  cashier;
	private String cashierName;
	 
	private BranchDTO branch;
	private Long branchId;  
	
	private PaymentType paymentType;
	
	private LocalDateTime createdAt;

}
