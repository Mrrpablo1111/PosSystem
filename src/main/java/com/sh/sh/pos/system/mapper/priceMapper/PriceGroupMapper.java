package com.sh.sh.pos.system.mapper.pricemapper;

import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.pricedto.PriceGroupDTO;

public class PriceGroupMapper {
    private PriceGroupMapper() {
    }

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
                .active(!Boolean.FALSE.equals(dto.getActive()))
                .build();
    }
}
