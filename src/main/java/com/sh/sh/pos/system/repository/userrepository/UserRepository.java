package com.sh.sh.pos.system.repository.userrepository;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;



public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    
    Set<User> findByRole(UserRole role);
    List<User> findByStoreId(Long storeId);
    List<User> findByBranchId(Long branchId);

    List<User> findByStoreAndRoleIn(Store store, List<UserRole> roles);
    List<User> findByBranchAndRoleIn(Branch branch, List<UserRole> roles);

   
}
