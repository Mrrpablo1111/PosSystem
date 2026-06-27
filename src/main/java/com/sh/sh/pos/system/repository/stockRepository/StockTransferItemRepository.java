package com.sh.sh.pos.system.repository.stockRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.stocks.StockTransferItem;

public interface StockTransferItemRepository extends JpaRepository<StockTransferItem,Long> {
    List<StockTransferItem> findByTransfer_Id(Long transferId);
}
