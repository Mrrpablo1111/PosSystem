package com.sh.sh.pos.system.security;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class StoreAuthorization {
    public boolean isStoreOwner(User user, Store store){
        return user != null
                && store != null
                && store.getStoreAdmin() != null
                && store.getStoreAdmin().getId().equals(user.getId());
    }

    public boolean isSuperAdmin(User user){
        return user != null && user.getRole() == UserRole.ROLE_ADMIN;
    }

    public void requirePermission(User user, String permission){
        if(user == null || user.getRole() == null){
            throw new UserException("Authentication required");
        }
        if(!RolePermissions.hasPermission(user.getRole(), permission)){
            throw new UserException("You do now have permission to perform this action");
        }
    }

    public void requireStoreOwnerOrSuperAdmin(User user, Store store){
        if (isSuperAdmin(user) || isStoreOwner(user, store)) {
            return;
        }

        throw new UserException(
                "You can only manage your own store"
        );
    }
    public void requireSuperAdmin(User user) {
        if (!isSuperAdmin(user)) {
            throw new UserException(
                    "Only the super admin can perform this action");
        }
    }
}
