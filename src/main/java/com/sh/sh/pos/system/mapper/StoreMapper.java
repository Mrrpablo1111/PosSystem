package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;


public class StoreMapper {
	public static StoreDTO toDTO(Store store) {
		StoreDTO storeDTO = new StoreDTO();
		
		storeDTO.setId(store.getId());
		storeDTO.setBrand(store.getBrand());
		storeDTO.setStoreAdmin(UserMapper.toDTO(store.getStoreAdmin()));
		storeDTO.setStoreType(store.getStoreType());
		storeDTO.setDescription(store.getDescription());
		storeDTO.setContact(store.getContact());
		storeDTO.setStoreType(store.getStoreType());
		storeDTO.setCreatedAt(store.getCreatedAt());
		storeDTO.setUpdatedAt(store.getUpdatedAt());
		storeDTO.setStatus(store.getStatus());
		return storeDTO;
		
	}
	
	public static Store toEntity(StoreDTO storeDTO, User storeAdmin) {
		
		Store store = new Store();
		store.setId(storeDTO.getId());
		store.setBrand(storeDTO.getBrand());
		store.setStoreAdmin(storeAdmin);
		store.setDescription(storeDTO.getDescription());
		store.setContact(storeDTO.getContact());
		store.setStatus(storeDTO.getStatus());
		store.setStoreType(store.getStoreType());
		store.setCreatedAt(storeDTO.getCreatedAt());
		store.setUpdatedAt(storeDTO.getUpdatedAt());
		store.setStatus(storeDTO.getStatus());
		return store;
	}
}
