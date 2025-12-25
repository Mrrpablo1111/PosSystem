package com.sh.sh.pos.system.service;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.payload.dto.RefundDTO;

public interface RefundService {
	
	RefundDTO createRefund(Refund refund) throws Exception;
	
	List<RefundDTO> getAllRefunds() throws Exception;
	
	RefundDTO getRefundByCashier(Long cashierId) throws Exception;
	RefundDTO getRefundByShiftReport(Long shiftReportId) throws Exception;
	
	List<RefundDTO> getRefundByCashierAndDateRang(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) throws Exception;
	
	List<RefundDTO> getRefundByBranch(Long branchId) throws Exception;
	
	RefundDTO getRefundById(Long refundId) throws Exception;
	
	void deleteRefund(Long refundId) throws Exception;
	
	

}
