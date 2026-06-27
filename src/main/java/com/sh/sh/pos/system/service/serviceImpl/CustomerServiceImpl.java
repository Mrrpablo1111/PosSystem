package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.mapper.CustomerMapper;
import com.sh.sh.pos.system.model.Customer;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.CustomerDTO;
import com.sh.sh.pos.system.repository.CustomerRepository;
import com.sh.sh.pos.system.repository.priceRepository.PriceGroupRepository;
import com.sh.sh.pos.system.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService{
	  private final CustomerRepository customerRepository;
    private final PriceGroupRepository priceGroupRepository;

    @Override
    public CustomerDTO createCustomer(CustomerDTO dto) {

        PriceGroup priceGroup = null;

        if (dto.getPriceGroupId() != null) {
            priceGroup = priceGroupRepository.findById(dto.getPriceGroupId())
                    .orElseThrow(() -> new RuntimeException("Price group not found"));
        }

        Customer customer = CustomerMapper.toEntity(dto, priceGroup);

        return CustomerMapper.toDTO(customerRepository.save(customer));
    }

    @Override
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) throws ResourceNotFoundException {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setEmail(dto.getEmail());
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());

        if (dto.getPriceGroupId() != null) {
            PriceGroup priceGroup = priceGroupRepository.findById(dto.getPriceGroupId())
                    .orElseThrow(() -> new RuntimeException("Price group not found"));

            customer.setPriceGroup(priceGroup);
        } else {
            customer.setPriceGroup(null);
        }

        return CustomerMapper.toDTO(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(Long id) throws ResourceNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found " + id));

        customerRepository.delete(customer);
    }

    @Override
    public CustomerDTO getCustomerById(Long id) throws ResourceNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));

        return CustomerMapper.toDTO(customer);
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::toDTO)
                .toList();
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        return customerRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(CustomerMapper::toDTO)
                .toList();
    }
}