package com.sh.sh.pos.system.repository.stockRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.domain.StockTransferStatus;
import com.sh.sh.pos.system.model.stocks.StockTransfer;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    List<StockTransfer> findByFromBranch_Id(Long branchId);

    List<StockTransfer> findByToBranch_Id(Long branchId);

    List<StockTransfer> findByStatus(StockTransferStatus status);

    List<StockTransfer> findByRequest_Id(Long requestId);
}
