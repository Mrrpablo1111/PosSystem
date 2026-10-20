package com.sh.sh.pos.system.mapper.storemapper;

import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.storecontact.StoreContact;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.storedto.StoreDTO;

public class StoreMapper {
    private StoreMapper() {
    }

    public static StoreDTO toDTO(Store store) {
        if (store == null)
            return null;
        return StoreDTO.builder()
                .id(store.getId())
                .brand(store.getBrand())
                .storeAdminId(store.getStoreAdmin() != null ? store.getStoreAdmin().getId() : null)
                .storeAdmin(store.getStoreAdmin() != null
                        ? UserMapper.toDTO(store.getStoreAdmin())
                        : null)
                .storeTypeId(store.getStoreType() != null ? store.getStoreType().getId() : null)
                .storeTypeName(store.getStoreType() != null ? store.getStoreType().getName() : null)
                .description(store.getDescription())
                .contact(store.getContact())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .status(store.getStatus())
                .build();

    }

    public static Store toEntity(StoreDTO storeDTO, User storeAdmin) {
        if (storeDTO == null)
            return null;

        return Store.builder()
                .brand(storeDTO.getBrand())
                .storeAdmin(storeAdmin)
                .contact(storeDTO.getContact())
                .description(storeDTO.getDescription())
                .build();
    }

    public static void updateEntity(Store store, StoreDTO storeDTO) {
        if(store == null || storeDTO == null){
            return;
        }
        if(storeDTO.getBrand() != null){
            store.setBrand(storeDTO.getBrand());
        }
        if (storeDTO.getDescription() != null) {
            store.setDescription(storeDTO.getDescription());
        }

        if (storeDTO.getContact() != null) {

            StoreContact contact = StoreContact.builder()
                    .address(storeDTO.getContact().getAddress())
                    .phone(storeDTO.getContact().getPhone())
                    .email(storeDTO.getContact().getEmail())
                    .build();

            store.setContact(contact);
        }

    }
}
