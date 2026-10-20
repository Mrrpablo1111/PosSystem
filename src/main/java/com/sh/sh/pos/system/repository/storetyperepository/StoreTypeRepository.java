package com.sh.sh.pos.system.repository.storetyperepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.sh.sh.pos.system.model.store.StoreType;


public interface StoreTypeRepository extends JpaRepository<StoreType, Long>{

    Optional<StoreType> findByCode(String code);

    Optional<StoreType> findByNameIgnoreCase(String name);

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    List<StoreType> findByIsActiveTrueOrderByNameAsc();

    List<StoreType> findAllByOrderByNameAsc();
}
