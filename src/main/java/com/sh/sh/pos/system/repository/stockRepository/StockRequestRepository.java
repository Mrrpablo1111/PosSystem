package com.sh.sh.pos.system.repository.stockRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.domain.StockRequestStatus;
import com.sh.sh.pos.system.model.stocks.StockRequest;

public interface StockRequestRepository extends JpaRepository<StockRequest,Long> {
    List<StockRequest> findByFromBranch_Id(Long branchId);
    List<StockRequest> findByToBranch_Id(Long branchId);
    List<StockRequest> findByStatus(StockRequestStatus status);
}
