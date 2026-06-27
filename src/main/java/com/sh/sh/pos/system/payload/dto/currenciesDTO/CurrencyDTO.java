package com.sh.sh.pos.system.payload.dto.currenciesDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {
    private Long id;

    private String code;
    private String name;
    
    private String symbol;
    private boolean active;
}
