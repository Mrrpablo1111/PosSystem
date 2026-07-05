package com.sh.sh.pos.system.model.paymentsummary;

import java.math.BigDecimal;

import com.sh.sh.pos.system.domain.PaymentType;

import lombok.Data;

@Data

public class PaymentSummary {

	private PaymentType type;
	
	private BigDecimal totalAmount;
	
	private int transactionCount;
	
	private BigDecimal percentage;
	
}
