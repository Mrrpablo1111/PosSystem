package com.sh.sh.pos.system.repository.suppliersRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.suppliers.Supplier;


public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCode(String code);

    List<Supplier> findByStoreId(Long storeId);

    boolean existsByCode(String code);
}