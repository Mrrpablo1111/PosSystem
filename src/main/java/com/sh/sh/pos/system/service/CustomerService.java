package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.model.Customer;

public interface CustomerService {
	Customer createCustomer(Customer customer);
	
	Customer updateCustomer(Long id, Customer customer) throws Exception;
	
	void deleteCustomer(Long id) throws Exception;
	
	Customer getCustomer(Long id) throws Exception;
	
	List<Customer> getAllCustomers();
	
	List<Customer> searchCustomers(String keyword);
}
