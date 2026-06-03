package com.sh.sh.pos.system.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.ShiftReportMapper;
import com.sh.sh.pos.system.model.ShiftReport;
import com.sh.sh.pos.system.payload.dto.ShiftReportDTO;
import com.sh.sh.pos.system.service.ShiftReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shift-reports")
public class ShiftReportController {

	private final ShiftReportService shiftReportService;
	private final ShiftReportMapper shiftReportMapper;

	// Start a new shift
	@PostMapping("/start")
	public ResponseEntity<ShiftReport> startShift(@RequestParam Long branchId) throws UserException {
		// currently user will be auto fetched from session service
		ShiftReport shift = shiftReportService.startShift(null, branchId, LocalDateTime.now());
		return ResponseEntity.ok(shift);
	}

	// End the current shift
	@PatchMapping("/end")
	public ResponseEntity<ShiftReportDTO> endShift() throws UserException {
		ShiftReport ended = shiftReportService.endShift(null, LocalDateTime.now());
		return ResponseEntity.ok(ShiftReportMapper.toDTO(ended));
	}

	// Get current shift progress for a cashier
	@GetMapping("/current")
	public ResponseEntity<ShiftReportDTO> getCurrentShiftProgress() throws UserException {
		ShiftReport currentShift = shiftReportService.getCurrentShiftProgress(null);
		return ResponseEntity.ok(ShiftReportMapper.toDTO(currentShift));
	}

	// Get shift report by Date(for cashier)
	@GetMapping("/cashier/{cashierId}/by-date")
	public ResponseEntity<ShiftReportDTO> getShiftReportByDate(@PathVariable Long cashierId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime date) {
		ShiftReport shiftReport = shiftReportService.getShiftByCashierAndDate(cashierId, date);
		return ResponseEntity.ok(ShiftReportMapper.toDTO(shiftReport));
	}

	// Get all shift report for a cashier
	@GetMapping("/cashier/{cashierId}")
	public ResponseEntity<List<ShiftReportDTO>> getShiftReportByCashierId(@PathVariable Long cashierId)
			{
		List<ShiftReport> shiftReport =  shiftReportService.getShiftReportByCashierId(cashierId);
		List<ShiftReportDTO> dto = shiftReport.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
		return ResponseEntity.ok(dto);
	}

	// Get all shift report for a branch
	@GetMapping("/branch/{branchId}")
	public ResponseEntity<List<ShiftReportDTO>> getShiftReportByBranchId(@PathVariable Long branchId) {
		List<ShiftReport> shiftReport = shiftReportService.getShiftReportByBranchId(branchId);
		List<ShiftReportDTO> dto = shiftReport.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
		return ResponseEntity.ok(dto);
	}

	//Get all  shift by Id
	@GetMapping("/{id}")
	public ResponseEntity<ShiftReportDTO> getShiftReportById(@PathVariable Long id){
		ShiftReport shiftReport = shiftReportService.getShiftReportById(id);
		return ResponseEntity.ok(ShiftReportMapper.toDTO(shiftReport));
	}

	// Get all shift reports (admin use)
	@GetMapping
	public ResponseEntity<List<ShiftReportDTO>> getAllShiftReports() {
		List<ShiftReport> shiftReports = shiftReportService.getAllShiftReports();
		List<ShiftReportDTO> dto = shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
		return ResponseEntity.ok(dto);
	}

	// Delete a shift report (admin use)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteShiftReport(@PathVariable Long id) {
		shiftReportService.deleteShiftReport(id);
		return ResponseEntity.noContent().build();
	}

}
