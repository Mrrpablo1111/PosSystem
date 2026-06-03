package com.sh.sh.pos.system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.model.Customer;

@Service
public interface CustomerService {
	Customer createCustomer(Customer customer);
	
	Customer updateCustomer(Long id, Customer customer) throws ResourceNotFoundException;
	
	void deleteCustomer(Long id) throws ResourceNotFoundException;
	
	Customer getCustomerById(Long id) throws ResourceNotFoundException;
	
	List<Customer> getAllCustomers();
	
	List<Customer> searchCustomers(String keyword);
}
