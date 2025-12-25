package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.payload.dto.RefundDTO;
import com.sh.sh.pos.system.service.RefundService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService{

	@Override
	public RefundDTO createRefund(Refund refund) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RefundDTO> getAllRefunds() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefundDTO getRefundByCashier(Long cashierId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefundDTO getRefundByShiftReport(Long shiftReportId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RefundDTO> getRefundByCashierAndDateRang(Long cashierId, LocalDateTime startDate, LocalDateTime endDate)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RefundDTO> getRefundByBranch(Long branchId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefundDTO getRefundById(Long refundId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteRefund(Long refundId) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
