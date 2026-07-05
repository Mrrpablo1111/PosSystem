package com.sh.sh.pos.system.repository.stockrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.stocks.StockRequestItem;

public interface StockRequestItemRepository extends JpaRepository<StockRequestItem, Long> {
    List<StockRequestItem> findByRequestId(Long requestId);
}
