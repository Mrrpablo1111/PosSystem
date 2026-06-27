package com.sh.sh.pos.system.repository.priceRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.price.PriceGroup;

public interface PriceGroupRepository extends JpaRepository<PriceGroup, Long> {
    Optional<PriceGroup> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
