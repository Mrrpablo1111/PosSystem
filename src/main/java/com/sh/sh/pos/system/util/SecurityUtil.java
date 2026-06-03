package com.sh.sh.pos.system.util;

import java.nio.file.AccessDeniedException;

import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.Inventory;
import com.sh.sh.pos.system.model.Product;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.service.UserService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SecurityUtil {

    private UserService userService;

    public void checkAuthority(Store store) throws AccessDeniedException, UserException{
        User user = userService.getCurrentUser();
        if(user.getRole() != UserRole.ROLE_BRANCH_MANAGER){
            throw new AccessDeniedException("Only Store manager can perform action");
        }
        if(user.getStore() == null || !user.getStore().getId().equals(store.getId())){
            throw new AccessDeniedException("you are authorized to manage this store.");
        }
    }

    public void checkAuthority(Branch branch) throws AccessDeniedException, UserException{
        checkAuthority(branch.getStore());
    }

    public void checkAuthority(Inventory inventory) throws AccessDeniedException, UserException{
        checkAuthority(inventory.getBranch());
    }

    public void checkAuthority(Product product) throws AccessDeniedException, UserException{
        checkAuthority(product.getStore());
    }


    
}
