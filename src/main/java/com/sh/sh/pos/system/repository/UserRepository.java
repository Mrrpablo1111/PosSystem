package com.sh.sh.pos.system.repository;

import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    
    List<User> findByStore(Store store);
    
    List<User> findByBranchId(Long branchId);

}
