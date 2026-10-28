package com.sh.sh.pos.system.mapper.storemapper;

import org.mapstruct.*;

import com.sh.sh.pos.system.model.store.StoreType;

import com.sh.sh.pos.system.payload.dto.storedto.StoreTypeDTO;


@Mapper(componentModel = "spring")
public interface StoreTypeMapper {
    StoreTypeDTO toDTO(StoreType storeType);

    @Mapping(target = "id", ignore  = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StoreType ToEntity(StoreTypeDTO storeTypeDTO);

    //Update existing entity from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target =  "updatedAt", ignore = true)
    void updateEntityFromDTO(
        StoreTypeDTO storeTypeDTO,
        @MappingTarget  StoreType storeType
    );
}
