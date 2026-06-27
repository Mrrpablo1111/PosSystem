package com.sh.sh.pos.system.mapper.priceMapper;

import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.priceDTO.PriceGroupDTO;

public class PriceGroupMapper {
     public static PriceGroupDTO toDTO(PriceGroup group) {
        return PriceGroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .active(group.getActive())
                .createdAt(group.getCreatedAt())
                .build();
    }

    public static PriceGroup toEntity(PriceGroupDTO dto) {
        return PriceGroup.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }
}
