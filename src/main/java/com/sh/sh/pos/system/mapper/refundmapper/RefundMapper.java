package com.sh.sh.pos.system.mapper.refundmapper;

import com.sh.sh.pos.system.model.refund.Refund;
import com.sh.sh.pos.system.payload.dto.refunddto.RefundDTO;

public class RefundMapper {
	private RefundMapper() {
	}

	public static RefundDTO toDTO(Refund refund) {

		RefundDTO dto = new RefundDTO();
		dto.setId(refund.getId());
		dto.setOrderId(refund.getOrder().getId());
		dto.setReason(refund.getReason());
		dto.setAmount(refund.getAmount());
		dto.setCashierName(refund.getCashier().getFullName());
		dto.setBranchId(refund.getBranch().getId());
		dto.setShiftReportId(refund.getShiftReport() != null ? refund.getShiftReport().getId() : null);
		dto.setCreatedAt(refund.getCreatedAt());
		return dto;

	}

}
