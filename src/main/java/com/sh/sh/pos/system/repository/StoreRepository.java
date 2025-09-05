package com.sh.sh.pos.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.Store;


public interface StoreRepository extends JpaRepository<Store, Long>{
	Store findByStoreAdminId(Long adminId);
	
}
