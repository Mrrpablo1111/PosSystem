package com.sh.sh.pos.system.payload.dto.exchangiesRateDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDTO {

    private Long id;

    private Long fromCurrencyId;

    private String fromCurrencyCode;

    private Long toCurrencyId;

    private String toCurrencyCode;

    private BigDecimal rate;

    private LocalDate rateDate;

    private Boolean active;

    private String source;
}