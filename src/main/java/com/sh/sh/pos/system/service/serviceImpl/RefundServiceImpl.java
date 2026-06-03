package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;


import com.sh.sh.pos.system.domain.OrderStatus;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Order;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.RefundDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.OrderRepository;
import com.sh.sh.pos.system.repository.RefundRepository;
import com.sh.sh.pos.system.service.RefundService;
import com.sh.sh.pos.system.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService{
	
	private final UserService userService;
	private final OrderRepository orderRepository;
	private final RefundRepository refundRepository;
	private final BranchRepository branchRepository;
	
	@Override
	@Transactional
	public Refund createRefund(RefundDTO refund) throws UserException, ResourceNotFoundException {
		User cashier = userService.getCurrentUser();
		
		Order order = orderRepository.findById(refund.getOrderId()).orElseThrow(() -> new ResourceNotFoundException("order not found"));
		
		Branch branch = branchRepository.findById(refund.getBranchId()).orElseThrow(()-> new EntityNotFoundException("brand not  found"));
		
		Refund createRefund = new Refund();
				createRefund.setOrder(order);
				createRefund.setCashier(cashier);
				createRefund.setBranch(branch);
				createRefund.setReason(refund.getReason());
				createRefund.setAmount(refund.getAmount());
				createRefund.setCreatedAt(refund.getCreatedAt());
		
		Refund savedRefund = refundRepository.save(createRefund);
 		
		order.setStatus(OrderStatus.REFUNDED);
		orderRepository.save(order);
		return savedRefund;
	}

	@Override
	public List<Refund> getAllRefunds(){
		return refundRepository.findAll();
		
	}

	@Override
	public List<Refund>  getRefundByCashier(Long cashierId) {
		
		return refundRepository.findByCashierId(cashierId);
	}

	@Override
	public List<Refund> getRefundByShiftReport(Long shiftReportId){
		return refundRepository.findByShiftReportId(shiftReportId);
	}

	@Override
	public List<Refund> getRefundByCashierAndDateRang(Long cashierId, LocalDateTime startDate, LocalDateTime endDate){
	
		return refundRepository.findByCashierIdAndCreatedAtBetween(cashierId, startDate, endDate);
	}

	@Override
	public List<Refund> getRefundByBranch(Long branchId){
		List<Refund> refunds = refundRepository.findByBranchId(branchId);
		
		return refunds;
	}

	@Override
	public Refund getRefundById(Long id) throws ResourceNotFoundException {
		
		
		return refundRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("refund not found"));
		
	}

	@Override
	public void deleteRefund(Long refundId) throws ResourceNotFoundException {
		if(!refundRepository.existsById(refundId)){
			throw new ResourceNotFoundException("Refund not found");
		}
		refundRepository.deleteById(refundId);
		
		
	}


}
