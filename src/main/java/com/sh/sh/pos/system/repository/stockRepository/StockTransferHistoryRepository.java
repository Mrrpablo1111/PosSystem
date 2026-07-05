package com.sh.sh.pos.system.repository.stockrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.stocks.StockTransferHistory;

public interface StockTransferHistoryRepository extends JpaRepository<StockTransferHistory, Long> {
    List<StockTransferHistory> findByTransferId(Long transferId);
}
