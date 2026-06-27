package com.sh.sh.pos.system.repository.warehousesRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.warehouses.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCode(String code);
    List<Warehouse> findByActiveTrue();
    List<Warehouse> findByBranchIdAndActiveTrue(Long branchId);
    Optional<Warehouse> findByBranchIdAndIsDefaultTrue(Long branchId);

}
