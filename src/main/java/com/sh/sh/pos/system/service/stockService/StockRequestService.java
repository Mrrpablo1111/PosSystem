package com.sh.sh.pos.system.service.stockService;

import java.util.List;

import com.sh.sh.pos.system.domain.StockRequestStatus;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockRequestDTO;

public interface StockRequestService {
    StockRequestDTO create(StockRequestDTO dto, User user);

    StockRequestDTO approve(Long id, User user);

    StockRequestDTO reject(Long id, User user);

    StockRequestDTO get(Long id);

    List<StockRequestDTO> getAll();

    List<StockRequestDTO> getByStatus(StockRequestStatus status);

    StockRequestDTO convertToTransfer(Long requestId, User user);
}
