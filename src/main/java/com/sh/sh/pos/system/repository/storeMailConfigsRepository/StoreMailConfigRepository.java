package com.sh.sh.pos.system.repository.storeMailConfigsRepository;

import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sh.sh.pos.system.model.storeMailConfigs.StoreMailConfig;
 

 
@Repository
public interface StoreMailConfigRepository extends JpaRepository<StoreMailConfig, Long> {
 
    Optional<StoreMailConfig> findByStoreId(Long storeId);
 
    Optional<StoreMailConfig> findByStoreIdAndActiveTrue(Long storeId);
}
 