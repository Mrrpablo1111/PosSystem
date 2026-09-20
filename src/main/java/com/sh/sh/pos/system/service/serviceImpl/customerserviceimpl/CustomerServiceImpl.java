package com.sh.sh.pos.system.service.serviceimpl.customerserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.mapper.customermapper.CustomerMapper;
import com.sh.sh.pos.system.model.customer.Customer;
import com.sh.sh.pos.system.model.price.PriceGroup;
import com.sh.sh.pos.system.payload.dto.customerdto.CustomerDTO;
import com.sh.sh.pos.system.repository.customerrepository.CustomerRepository;
import com.sh.sh.pos.system.repository.pricerepository.PriceGroupRepository;
import com.sh.sh.pos.system.service.customerservice.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PriceGroupRepository priceGroupRepository;

    @Override
    @Transactional
    public CustomerDTO createCustomer(CustomerDTO dto) {
        PriceGroup priceGroup = resolvePriceGroup(dto.getPriceGroupId());

        Customer customer = CustomerMapper.toEntity(dto, priceGroup);

        return CustomerMapper.toDTO(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = findCustomerOrThrow(id);

        customer.setEmail(dto.getEmail());
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());
        customer.setPriceGroup(resolvePriceGroup(dto.getPriceGroupId()));

        return CustomerMapper.toDTO(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.delete(findCustomerOrThrow(id));
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        return CustomerMapper.toDTO(findCustomerOrThrow(id));
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

    // ── Helpers ───────────────────────────────────────────────────────────

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + id));
    }

    private PriceGroup resolvePriceGroup(Long priceGroupId) {
        if (priceGroupId == null) {
            return null;
        }
        return priceGroupRepository.findById(priceGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Price group not found with id: " + priceGroupId));
    }
}