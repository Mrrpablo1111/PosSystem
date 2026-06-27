package com.sh.sh.pos.system.payload.dto.priceDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceGroupDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
}
