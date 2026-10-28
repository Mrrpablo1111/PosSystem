package com.sh.sh.pos.system.mapper.storemapper;

import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.store.StoreType;
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
                .storeTypeId(store.getStoreType() != null ? store.getStoreType().getId() : null)
                .storeTypeName(store.getStoreType() != null ? store.getStoreType().getName() : null)
                .description(store.getDescription())
                .contact(store.getContact())
                .status(store.getStatus())
                .storeAdminId(store.getStoreAdmin() != null ? store.getStoreAdmin().getId() : null)
                .storeAdmin(store.getStoreAdmin() != null
                        ? UserMapper.toDTO(store.getStoreAdmin())
                        : null)
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();

    }

    public static Store toEntity(StoreDTO storeDTO, User storeAdmin) {
        if (storeDTO == null)
            return null;

        return Store.builder()
                .brand(storeDTO.getBrand())
                .contact(storeDTO.getContact())
                .description(storeDTO.getDescription())
                .storeAdmin(storeAdmin)
                .build();
    }

    public static void updateEntity(Store store, StoreDTO storeDTO) {
        if (store == null || storeDTO != null) {
            return;
        }
        if (storeDTO.getContact() != null) {

            StoreContact existing = store.getContact();
            StoreContact incoming = storeDTO.getContact();

            if (existing == null) {
                // The store has no contact yet, so create one.
                store.setContact(copyContact(incoming));

            } else {
                // The store already has contact information.
                // Update only the fields provided.

                if (incoming.getAddress() != null) {
                    existing.setAddress(incoming.getAddress());
                }

                if (incoming.getEmail() != null) {
                    existing.setEmail(incoming.getEmail());
                }

                if (incoming.getPhone() != null) {
                    existing.setPhone(incoming.getPhone());
                }
            }
        }
    }

    private static StoreContact copyContact(StoreContact contact) {
        if (contact == null) {
            return null;
        }
        return StoreContact.builder()
                .address(contact.getAddress())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .build();
    }

    //Call from the service after loading a valid StoreType
    public static void updateStoreType(Store store, StoreType storeType) {
        if (store != null && storeType != null) {
            store.setStoreType(storeType);
        }
    }

}
