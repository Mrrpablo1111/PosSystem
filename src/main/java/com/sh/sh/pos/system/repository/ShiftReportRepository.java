package com.sh.sh.pos.system.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.ShiftReport;
import com.sh.sh.pos.system.model.User;

public interface ShiftReportRepository extends JpaRepository<ShiftReport, Long> {
	
	List<ShiftReport> findByCashierId(Long id);
	
	List<ShiftReport> findByBranchId(Long id);
	
	Optional<ShiftReport> findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(User cashier);
	
	Optional<ShiftReport> findByCashierAndShiftStartBetween(User cashier, LocalDateTime start, LocalDateTime end);

}
