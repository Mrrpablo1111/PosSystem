package com.sh.sh.pos.system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.Refund;
import com.sh.sh.pos.system.model.User;

public interface RefundRepository extends JpaRepository<Refund, Long> {
	
	List<Refund> findByCashierAndCreatedAtBetween(User cashier, LocalDateTime start, LocalDateTime end );
	List<Refund> findByCashierId(Long cashierId);
	List<Refund> findByCashierIdAndCreatedAtBetween(Long cashierId, LocalDateTime start, LocalDateTime end);
	List<Refund> findByShiftReportId(Long shiftReportId);
	List<Refund> findByBranchId(Long bracnchId); 

}
