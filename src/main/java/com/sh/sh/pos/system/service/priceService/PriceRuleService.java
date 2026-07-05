package com.sh.sh.pos.system.service.priceservice;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.pricedto.PriceRuleDTO;

public interface PriceRuleService {
    PriceRuleDTO create(PriceRuleDTO dto);

    PriceRuleDTO update(Long id, PriceRuleDTO dto);

    PriceRuleDTO get(Long id);

    List<PriceRuleDTO> getAll();

    List<PriceRuleDTO> getByProduct(Long productId);

    List<PriceRuleDTO> getByPriceGroup(Long priceGroupId);

    void delete(Long id);
}
