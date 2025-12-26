package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.RefundMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.RefundDTO;
import com.sh.sh.pos.system.repository.OrderRepository;
import com.sh.sh.pos.system.repository.RefundRepository;
import com.sh.sh.pos.system.service.RefundService;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService{
	
	private final UserService userService;
	private final OrderRepository orderRepository;
	private final RefundRepository refundRepository;
	
	@Override
	public RefundDTO createRefund(RefundDTO refund) throws Exception {
		User cashier = userService.getCurrentUser();
		
		Order order = orderRepository.findById(refund.getOrderId()).orElseThrow(() -> new Exception("order not found"));
		
		Branch branch = order.getBranch();
		
		Refund createRefund = Refund.builder()
				.order(order)
				.cashier(cashier)
				.branch(branch)
				.reason(refund.getReason())
				.amount(refund.getAmount())
				.createdAt(refund.getCreatedAt())
				.build();
		
		Refund savedRefund = refundRepository.save(createRefund);
 				 
		return RefundMapper.toDTO(savedRefund);
	}

	@Override
	public List<RefundDTO> getAllRefunds() throws Exception {
		return refundRepository.findAll().stream().map(RefundMapper::toDTO).collect(Collectors.toList());
		
	}

	@Override
	public List<RefundDTO>  getRefundByCashier(Long cashierId) throws Exception {
		
		return refundRepository.findByCashierId(cashierId).stream().map(RefundMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<RefundDTO> getRefundByShiftReport(Long shiftReportId) throws Exception {
		return refundRepository.findByShiftReportId(shiftReportId).stream().map(RefundMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<RefundDTO> getRefundByCashierAndDateRang(Long cashierId, LocalDateTime startDate, LocalDateTime endDate)
			throws Exception {
	
		return refundRepository.findByCashierIdAndCreatedAtBetween(cashierId, startDate, endDate).stream().map(RefundMapper::toDTO).collect(Collectors.toList()) ;
	}

	@Override
	public List<RefundDTO> getRefundByBranch(Long branchId) throws Exception {
		
		return refundRepository.findByBranchId(branchId).stream().map(RefundMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public RefundDTO getRefundById(Long refundId) throws Exception {
		
		
		return refundRepository.findById(refundId).map(RefundMapper::toDTO).orElseThrow(()-> new Exception("Refund not found"));
		
	}

	@Override
	public void deleteRefund(Long refundId) throws Exception {
		this.getRefundById(refundId);
		refundRepository.deleteById(refundId);
		
		
	}


}
