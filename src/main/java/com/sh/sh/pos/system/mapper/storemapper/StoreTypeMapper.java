package com.sh.sh.pos.system.mapper.storemapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.sh.sh.pos.system.model.store.StoreType;
import com.sh.sh.pos.system.payload.dto.storedto.StoreDTO;
import com.sh.sh.pos.system.payload.dto.storedto.StoreTypeDTO;

@Mapper(componentModel = "spring")
public interface StoreTypeMapper {
    StoreTypeDTO toDTO(StoreType storeType);

    StoreType ToEntity(StoreTypeDTO storeTypeDTO);

    void updateEntityFromDTO(
        StoreTypeDTO storeTypeDTO,
        @MappingTarget  StoreType storeType
    );
}
