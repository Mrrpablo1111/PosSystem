package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.Customer;
import com.sh.sh.pos.system.repository.CustomerRepository;
import com.sh.sh.pos.system.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService{
	private final CustomerRepository customerRepository;

	@Override
	public Customer createCustomer(Customer customer) {
		 
		return customerRepository.save(customer);
	}

	@Override
	public Customer updateCustomer(Long id, Customer customer) throws Exception {
		Customer customerToUpdate = customerRepository.findById(id).orElseThrow(
				() -> new Exception("Customer not found")); 
		
		customerToUpdate.setEmail(customer.getEmail());
		customerToUpdate.setFullName(customer.getFullName());
		customerToUpdate.setPhone(customer.getPhone());
		
		return customerRepository.save(customer);
 	}

	@Override
	public void deleteCustomer(Long id) throws Exception {
		Customer customerToUpdate = customerRepository.findById(id).orElseThrow(
				() -> new Exception("Customer not found")); 
		
		customerRepository.delete(customerToUpdate);
	}
	
	@Override
	public Customer getCustomer(Long id) throws Exception {
		
		return customerRepository.findById(id).orElseThrow(()-> new Exception("Customer not found"));
	}

	@Override
	public List<Customer> getAllCustomers() {
		 
		return customerRepository.findAll();
	}

	@Override
	public List<Customer> searchCustomers(String keyword) {
		
		return customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
	}

	

}
