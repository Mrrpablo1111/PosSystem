package com.sh.sh.pos.system.repository.customerrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.customer.Customer;


public interface CustomerRepository extends JpaRepository<Customer,Long> {
	
	List<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email); 

}
