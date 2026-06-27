package com.sh.sh.pos.system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;

import com.sh.sh.pos.system.payload.dto.CustomerDTO;

@Service
public interface CustomerService {
	CustomerDTO createCustomer(CustomerDTO dto);

    CustomerDTO updateCustomer(Long id, CustomerDTO dto)
            throws ResourceNotFoundException;

    void deleteCustomer(Long id)
            throws ResourceNotFoundException;

    CustomerDTO getCustomerById(Long id)
            throws ResourceNotFoundException;

    List<CustomerDTO> getAllCustomers();

    List<CustomerDTO> searchCustomers(String keyword);
}
