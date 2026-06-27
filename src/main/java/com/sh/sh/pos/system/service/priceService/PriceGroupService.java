package com.sh.sh.pos.system.service.priceService;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.priceDTO.PriceGroupDTO;

public interface PriceGroupService {
    PriceGroupDTO create(PriceGroupDTO dto);

    PriceGroupDTO update(Long id, PriceGroupDTO dto);

    PriceGroupDTO get(Long id);

    List<PriceGroupDTO> getAll();

    void delete(Long id);
}
