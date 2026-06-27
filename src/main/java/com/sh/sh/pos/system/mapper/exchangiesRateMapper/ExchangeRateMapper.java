package com.sh.sh.pos.system.mapper.exchangiesRateMapper;

import java.util.List;

import com.sh.sh.pos.system.model.exchangiesRate.ExchangeRate;
import com.sh.sh.pos.system.payload.dto.exchangiesRateDTO.ExchangeRateDTO;

public class ExchangeRateMapper {

    public static ExchangeRateDTO toDTO(ExchangeRate entity) {

        ExchangeRateDTO dto = new ExchangeRateDTO();

        dto.setId(entity.getId());

        dto.setFromCurrencyId(entity.getFromCurrency().getId());
        dto.setFromCurrencyCode(entity.getFromCurrency().getCode());

        dto.setToCurrencyId(entity.getToCurrency().getId());
        dto.setToCurrencyCode(entity.getToCurrency().getCode());

        dto.setRate(entity.getRate());

        dto.setRateDate(entity.getRateDate());

        dto.setActive(entity.getActive());

        dto.setSource(entity.getSource());

        return dto;
    }

    public static List<ExchangeRateDTO> toDTOList(
            List<ExchangeRate> entities) {

        return entities.stream()
                .map(ExchangeRateMapper::toDTO)
                .toList();
    }
}