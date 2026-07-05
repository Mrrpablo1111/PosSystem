package com.sh.sh.pos.system.repository.userrepository;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.user.User;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;



public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    Set<User> findByRole(UserRole role);
    List<User> findByStoreId(Long storeId);
    List<User> findByBranchId(Long branchId);

    List<User> findByStoreAndRoleIn(com.sh.sh.pos.system.model.store.Store store, List<UserRole> roles);
    List<User> findByBranchAndRoleIn(com.sh.sh.pos.system.model.branch.Branch branch, List<UserRole> roles);
}
