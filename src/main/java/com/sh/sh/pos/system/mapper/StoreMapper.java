 package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.StoreDTO;


public class StoreMapper {
	public static StoreDTO toDTO(Store store) {
		return StoreDTO.builder()
                    .id(store.getId())
                    .brand(store.getBrand())
                    .storeAdminId(store.getStoreAdmin() != null ? store.getStoreAdmin().getId() : null)
                    .storeAdmin(UserMapper.toDTO(store.getStoreAdmin()))
                    .storeType(store.getStoreType())
                    .description(store.getDescription())
                    .contact(store.getContact())
                    .createdAt(store.getCreatedAt())
                    .updatedAt(store.getUpdatedAt())
                    .status(store.getStatus())
                    .build();
		
	}
	
	public static Store toEntity(StoreDTO storeDTO, User storeAdmin) {
        
		
		    return Store.builder()
                    .id(storeDTO.getId())
                    .brand(storeDTO.getBrand())
                    .storeAdmin(storeAdmin)
                    .createdAt(storeDTO.getCreatedAt())
                    .updatedAt(storeDTO.getUpdatedAt())
                    .storeType(storeDTO.getStoreType())
                    .contact(storeDTO.getContact())
                    .description(storeDTO.getDescription())
                    .build();
	}
}
