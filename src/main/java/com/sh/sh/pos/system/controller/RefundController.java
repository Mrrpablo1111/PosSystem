package com.sh.sh.pos.system.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.RefundMapper;
import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.payload.dto.RefundDTO;
import com.sh.sh.pos.system.service.RefundService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refunds")


public class RefundController {
	
	private final RefundService refundService;
	
	//Create a new refund
	@PostMapping
	public ResponseEntity<RefundDTO> createRefund(@RequestBody RefundDTO refundDTO) throws UserException, ResourceNotFoundException{
		
		Refund refund = refundService.createRefund(refundDTO);
		
		return ResponseEntity.ok(RefundMapper.toDTO(refund));
	}

	//Get all refund(admin)
	@GetMapping
	public ResponseEntity<List<RefundDTO>> getAllRefund(){
		
		List<RefundDTO> refund = refundService.getAllRefunds().stream().map(RefundMapper::toDTO).collect(Collectors.toList());
		
		return ResponseEntity.ok(refund);
	}
	
	//Get refund by cashier
	@GetMapping("/cashier/{cashierId}")
	public ResponseEntity<List<RefundDTO>> getRefundByCashier(@PathVariable Long cashierId){
		
		List<RefundDTO> refund = refundService.getRefundByCashier(cashierId).stream().map(RefundMapper::toDTO).collect(Collectors.toList());
		
		return ResponseEntity.ok(refund);
	}
	
	//Get refund by branch
	@GetMapping("/branch/{branchId}")
	public ResponseEntity<List<RefundDTO>> getRefundByBranch(@PathVariable Long branchId){
		List<RefundDTO> refund = refundService.getRefundByBranch(branchId).stream().map(RefundMapper::toDTO).collect(Collectors.toList());
		return ResponseEntity.ok(refund);
	}
	
	//Get refund by shift report
	@GetMapping("/shift/{shiftReportId}")
	public ResponseEntity<List<RefundDTO>> getRefundByShift(@PathVariable Long shiftReportId) {
		
		List<RefundDTO> refund = refundService.getRefundByShiftReport(shiftReportId).stream().map(RefundMapper::toDTO).collect(Collectors.toList());
		
		return ResponseEntity.ok(refund);
	}
	
	
	//Get refund by cashier and date range
	@GetMapping("/cashier/{cashierId}/range")
	public ResponseEntity<List<RefundDTO>> getRefundByCashierAndDateRange(@PathVariable Long cashierId,
																		  @PathVariable @DateTimeFormat (iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
																		  @PathVariable @DateTimeFormat (iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate 
			){
		
		List<RefundDTO> refund = refundService.getRefundByCashierAndDateRang(cashierId, startDate, endDate).stream().map(RefundMapper::toDTO).collect(Collectors.toList());
	 
		
		return ResponseEntity.ok(refund);
	}
	
	//Get refund by id
	@GetMapping("/{id}")
	public ResponseEntity<RefundDTO> getRefundById(@PathVariable Long id) throws ResourceNotFoundException{
		
		Refund refund = refundService.getRefundById(id);
		
		return ResponseEntity.ok(RefundMapper.toDTO(refund));
	}
	
	//Delete refund by id
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteRefundById(@PathVariable Long id) throws ResourceNotFoundException{
		refundService.deleteRefund(id);
		return ResponseEntity.ok("Refund deleted successfully.");
	}
	
	

}
