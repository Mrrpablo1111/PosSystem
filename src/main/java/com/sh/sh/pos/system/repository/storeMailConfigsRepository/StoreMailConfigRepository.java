package com.sh.sh.pos.system.repository.storemailconfigsrepository;

import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.storemailconfigs.StoreMailConfig;
 

public interface StoreMailConfigRepository extends JpaRepository<StoreMailConfig, Long> {
 
    Optional<StoreMailConfig> findByStoreId(Long storeId);
 
    Optional<StoreMailConfig> findByStoreIdAndActiveTrue(Long storeId);
}
 