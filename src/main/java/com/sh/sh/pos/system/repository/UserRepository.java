package com.sh.sh.pos.system.repository;

import com.sh.sh.pos.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

}
