package com.sh.sh.pos.system.repository.stockrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.domain.StockTransferStatus;
import com.sh.sh.pos.system.model.stocks.StockTransfer;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    List<StockTransfer> findByFromBranchId(Long branchId);

    List<StockTransfer> findByToBranchId(Long branchId);

    List<StockTransfer> findByStatus(StockTransferStatus status);

    List<StockTransfer> findByRequestId(Long requestId);
}
