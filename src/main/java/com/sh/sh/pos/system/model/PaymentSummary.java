package com.sh.sh.pos.system.model;

import com.sh.sh.pos.system.domain.PaymentType;

import lombok.Data;

@Data

public class PaymentSummary {

	private PaymentType Type;
	
	private Double totalAmount;
	
	private int transactionCount;
	
	private double percentage;
	
}
