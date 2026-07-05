package com.sh.sh.pos.system.repository.shiftreportrepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.shiftreport.ShiftReport;
import com.sh.sh.pos.system.model.user.User;

public interface ShiftReportRepository extends JpaRepository<ShiftReport, Long> {
	
	List<ShiftReport> findByCashier(User cashier);
	
	List<ShiftReport> findByBranch(Branch branch);
	
	Optional<ShiftReport> findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(User cashier);
	
	Optional<ShiftReport> findByCashierAndShiftStartBetween(User cashier, LocalDateTime start, LocalDateTime end);

}
