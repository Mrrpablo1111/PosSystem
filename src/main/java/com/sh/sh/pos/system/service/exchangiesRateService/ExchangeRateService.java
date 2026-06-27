package com.sh.sh.pos.system.service.exchangiesRateService;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.exchangiesRateDTO.ExchangeRateDTO;

public interface ExchangeRateService {

    ExchangeRateDTO create(ExchangeRateDTO dto);

    ExchangeRateDTO update(Long id, ExchangeRateDTO dto);

    ExchangeRateDTO getById(Long id);

    List<ExchangeRateDTO> getAll(Boolean active);

    void activate(Long id);

    void deactivate(Long id);

    ExchangeRateDTO getLatestRate(
            String fromCurrency,
            String toCurrency);
}