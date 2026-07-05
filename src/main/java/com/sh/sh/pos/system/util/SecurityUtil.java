package com.sh.sh.pos.system.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.inventory.Inventory;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.service.userservice.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserService userService;

    public void checkAuthority(Store store) {
        User user = userService.getCurrentUser();

        boolean isStoreLevelRole = user.getRole() == UserRole.ROLE_STORE_ADMIN
                || user.getRole() == UserRole.ROLE_STORE_MANAGER
                || user.getRole() == UserRole.ROLE_BRANCH_MANAGER;

        if (!isStoreLevelRole) {
            throw new AccessDeniedException("Your role is not permitted to perform this action");
        }
        if (user.getStore() == null || !user.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException("You are not authorized to manage this store");
        }
    }

    public void checkAuthority(Branch branch) {
        checkAuthority(branch.getStore());
    }

    public void checkAuthority(Inventory inventory) {
        checkAuthority(inventory.getBranch());
    }

    public void checkAuthority(Product product) {
        checkAuthority(product.getStore());
    }
}