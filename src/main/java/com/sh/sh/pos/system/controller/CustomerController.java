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

import com.sh.sh.pos.system.model.Customer;
import com.sh.sh.pos.system.payload.response.ApiResponse;
import com.sh.sh.pos.system.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {
	
	private final CustomerService customerService;
	
	@PostMapping()
	public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer){
		
		return ResponseEntity.ok(customerService.createCustomer(customer));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) throws Exception{
		return ResponseEntity.ok(customerService.updateCustomer(id, customer));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable Long id) throws Exception{
		customerService.deleteCustomer(id);
		
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMessage("Customer deleted");
		
		return ResponseEntity.ok(apiResponse);
	}
	
	@GetMapping()
	public ResponseEntity<List<Customer>> getAll(){
		return ResponseEntity.ok(customerService.getAllCustomers());
	}
	
	@GetMapping("/search")
	public ResponseEntity<List<Customer>> search(@RequestParam String q){
		return ResponseEntity.ok(customerService.searchCustomers(q));
	}

}
