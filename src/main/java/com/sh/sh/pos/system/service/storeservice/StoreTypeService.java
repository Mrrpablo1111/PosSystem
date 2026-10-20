package com.sh.sh.pos.system.service.storeservice;

import java.util.List;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.storedto.StoreTypeDTO;

public interface StoreTypeService {

    StoreTypeDTO createStoreType(StoreTypeDTO storeTypeDTO) throws UserException;

    StoreTypeDTO getStoreTypeById(Long Id) throws UserException;

    List<StoreTypeDTO> getAllStoreTypes();

    List<StoreTypeDTO> getActiveStoreTypes();

    StoreTypeDTO updateStoreType(Long id, StoreTypeDTO storeTypeDTO) throws UserException;

    StoreTypeDTO deactivateStoreType(Long id) throws UserException;

    StoreTypeDTO activateStoreType(Long id) throws UserException;

}
