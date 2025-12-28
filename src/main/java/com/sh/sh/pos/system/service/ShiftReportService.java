package com.sh.sh.pos.system.service;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.payload.dto.ShiftReportDTO;

public interface ShiftReportService {
	
	ShiftReportDTO startShift() throws Exception;
	
	ShiftReportDTO endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception;
	
	ShiftReportDTO getShiftReportById(Long id) throws Exception;
	
	List<ShiftReportDTO> getAllShiftReports();
	
	List<ShiftReportDTO> getShiftReportByBranchId(Long branchId);
	
	List<ShiftReportDTO> getShiftReportByCashierId(Long cashierId);
	
	ShiftReportDTO getCurrentShiftProgress(Long cashierId) throws Exception;
	
	ShiftReportDTO getShiftByCashierAndDate(Long cashierId, LocalDateTime date) throws Exception;
	 
	

}
