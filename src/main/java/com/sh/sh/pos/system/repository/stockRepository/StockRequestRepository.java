package com.sh.sh.pos.system.repository.stockrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.domain.StockRequestStatus;
import com.sh.sh.pos.system.model.stocks.StockRequest;

public interface StockRequestRepository extends JpaRepository<StockRequest,Long> {
    List<StockRequest> findByFromBranchId(Long branchId);
    List<StockRequest> findByToBranchId(Long branchId);
    List<StockRequest> findByStatus(StockRequestStatus status);
}
