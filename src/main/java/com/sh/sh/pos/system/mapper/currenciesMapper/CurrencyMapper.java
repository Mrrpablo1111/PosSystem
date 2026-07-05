package com.sh.sh.pos.system.mapper.currenciesmapper;

import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.payload.dto.currenciesdto.CurrencyDTO;
import com.sh.sh.pos.system.payload.request.CurrencyRequestDTO;

@Component
public class CurrencyMapper {
    private CurrencyMapper() {
    }

    public CurrencyDTO toDTO(Currency c) {
        return CurrencyDTO.builder()
                .id(c.getId())
                .code(c.getCode().toUpperCase())
                .name(c.getName())
                .symbol(c.getSymbol())
                .active(Boolean.TRUE.equals(c.getActive()))
                .build();
    }

    public Currency toEntity(CurrencyRequestDTO req) {
        return Currency.builder()
                .code(req.getCode().toUpperCase().trim())
                .name(req.getName().trim())
                .symbol(req.getSymbol().trim())
                .active(!Boolean.FALSE.equals(req.getActive()))
                .build();
    }

    public void updateEntity(Currency c, CurrencyRequestDTO req) {
        c.setCode(req.getCode().toUpperCase().trim());
        c.setName(req.getName().trim());
        c.setSymbol(req.getSymbol().trim());
        if (req.getActive() != null)
            c.setActive(req.getActive());
    }
}