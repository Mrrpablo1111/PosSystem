package com.sh.sh.pos.system.service;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.ShiftReport;

public interface ShiftReportService {
	
	ShiftReport startShift(Long cashierId, Long branchId, LocalDateTime shiftStart) throws UserException;
	
	ShiftReport endShift(Long shiftReportId, LocalDateTime shiftEnd) throws UserException;
	
	ShiftReport getShiftReportById(Long id) ;

	
	List<ShiftReport> getAllShiftReports();
	
	List<ShiftReport> getShiftReportByBranchId(Long branchId);
	
	List<ShiftReport> getShiftReportByCashierId(Long cashierId);
	
	ShiftReport getCurrentShiftProgress(Long cashierId) throws UserException;

	
	
	ShiftReport getShiftByCashierAndDate(Long cashierId, LocalDateTime date);
	 
	void deleteShiftReport(Long id);

}
