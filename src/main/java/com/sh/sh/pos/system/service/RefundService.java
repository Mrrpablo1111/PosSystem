package com.sh.sh.pos.system.service;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.payload.dto.RefundDTO;

public interface RefundService {
	
	Refund createRefund(RefundDTO refund) throws UserException, ResourceNotFoundException;
	
	List<Refund> getAllRefunds();
	
	List<Refund> getRefundByCashier(Long cashierId);
    List<Refund> getRefundByShiftReport(Long shiftReportId);
	
	List<Refund> getRefundByCashierAndDateRang(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) ;
	
	List<Refund> getRefundByBranch(Long branchId);
	
	Refund getRefundById(Long refundId) throws ResourceNotFoundException;
	
	void deleteRefund(Long refundId) throws ResourceNotFoundException;
	
	

}
