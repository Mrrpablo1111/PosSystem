package com.sh.sh.pos.system.service.stockService;

import java.util.List;

import com.sh.sh.pos.system.domain.StockTransferStatus;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockTransferDTO;

public interface StockTransferService {
    StockTransferDTO create(StockTransferDTO dto, User user);

    StockTransferDTO approve(Long id, User user);

    StockTransferDTO markPicking(Long id, User user);

    StockTransferDTO markPacked(Long id, User user);

    StockTransferDTO ship(Long id, StockTransferDTO shipmentDto, User user);

    StockTransferDTO receive(Long id, StockTransferDTO receiveDto, User user);

    StockTransferDTO get(Long id);

    List<StockTransferDTO> getAll();

    List<StockTransferDTO> getByStatus(StockTransferStatus status);

    void cancel(Long id, User user);
}
