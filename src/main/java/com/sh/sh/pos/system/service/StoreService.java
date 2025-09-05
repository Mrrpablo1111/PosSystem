package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;

public interface StoreService {
	StoreDTO createStore(StoreDTO storeDTO, User user);
	StoreDTO getStoreById(Long id) throws Exception;
	
	List<StoreDTO> getAllStores();
	Store getStoreByAdmin() throws UserException;
	StoreDTO updateStore(Long id, StoreDTO storeDTO) throws UserException;
	StoreDTO deletedStore(Long id);
	StoreDTO getStoreByEmployee() throws UserException;
	
	
	
	
}
