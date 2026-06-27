package com.sh.sh.pos.system.service.priceService;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.priceDTO.PriceRuleDTO;

public interface PriceRuleService {
    PriceRuleDTO create(PriceRuleDTO dto);

    PriceRuleDTO update(Long id, PriceRuleDTO dto);

    PriceRuleDTO get(Long id);

    List<PriceRuleDTO> getAll();

    List<PriceRuleDTO> getByProduct(Long productId);

    List<PriceRuleDTO> getByPriceGroup(Long priceGroupId);

    void delete(Long id);
}
