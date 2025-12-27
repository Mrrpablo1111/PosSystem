package com.sh.sh.pos.system.service;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.payload.dto.ShiftReportDTO;

public interface ShiftReportService {
	
	ShiftReportDTO startShift(Long cashierId, Long branchId, LocalDateTime shiftStart) throws Exception;
	
	ShiftReportDTO endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception;
	
	ShiftReportDTO getShiftReportId(Long id);
	
	List<ShiftReportDTO> getAllShiftReports();
	
	List<ShiftReportDTO> getShiftReportByBranchId(Long branchId);
	
	List<ShiftReportDTO> getShiftReportByCashierId(Long cashierId);
	
	ShiftReportDTO getCurrentShiftProgress(Long cashierId) throws Exception;
	
	ShiftReportDTO getShiftByCashierAndDate(Long cashierId, LocalDateTime date) throws Exception;
	 
	

}
