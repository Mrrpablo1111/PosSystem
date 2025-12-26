package com.sh.sh.pos.system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.Refund;

public interface RefundRepository extends JpaRepository<Refund, Long> {
	
	List<Refund> findByCashierIdAndCreatedAtBetween(Long cashier, LocalDateTime from, LocalDateTime To );
	List<Refund> findByCashierId(Long id);
	
	List<Refund> findByShiftReportId(Long id);
	List<Refund> findByBranchId(Long id); 

}
