package com.sh.sh.pos.system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.model.Customer;
import com.sh.sh.pos.system.payload.dto.CustomerDTO;
import com.sh.sh.pos.system.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {
	
	private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(customerService.createCustomer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerDTO dto) throws ResourceNotFoundException {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getById(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) throws ResourceNotFoundException {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(customerService.searchCustomers(q));
    }

}
