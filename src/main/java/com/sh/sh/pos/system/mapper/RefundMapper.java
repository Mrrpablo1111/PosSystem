package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.payload.dto.RefundDTO;

public class RefundMapper {

	public static RefundDTO toDTO(Refund refund) {
		
		
		return RefundDTO.builder()
				.id(refund.getId())
				.orderId(refund.getOrder().getId())
				.reason(refund.getReason())
				.amount(refund.getAmount())
				.cashierName(refund.getCashier().getFullName())
				.branchId(refund.getBranch().getId())
				.shiftReportId(refund.getShiftReport().getId())
				.createdAt(refund.getCreatedAt())
				.build();
		
	}

}
