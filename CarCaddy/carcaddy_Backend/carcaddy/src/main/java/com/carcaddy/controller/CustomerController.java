package com.carcaddy.controller;

import com.carcaddy.dto.CustomerDTO;
import com.carcaddy.entity.Customer;
import com.carcaddy.service.ICustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin
public class CustomerController {

    private final ICustomerService service;

    // Add new customer

    @PostMapping
    public ResponseEntity<Customer> addCustomer(@Valid @RequestBody CustomerDTO dto) {
        return new ResponseEntity<>(service.addCustomer(dto), HttpStatus.CREATED);
    }


    // Update customer details
    @PutMapping("/{id}")
    public Customer updateCustomer(
            @PathVariable String id,
            @RequestBody CustomerDTO dto) {

        return service.updateCustomer(id, dto);
    }

    // Update contact number
    @PatchMapping("/{id}/contact/{contact}")
    public Customer updateContact(
            @PathVariable String id,
            @PathVariable String contact) {

        return service.updateContact(id, contact);
    }

    // Get all customers
    @GetMapping
    public List<Customer> getAll() {
        return service.getAllCustomers();
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public Customer getById(@PathVariable String id) {
        return service.getCustomerById(id);
    }

    // Search customers by name
    @GetMapping("/search/{name}")
    public List<Customer> getByName(@PathVariable String name) {
        return service.getCustomersByName(name);
    }

    // Blacklist customer
    @PatchMapping("/{id}/blacklist")
    public Customer blacklist(@PathVariable String id) {
        return service.blacklistCustomer(id);
    }

    
    
    @GetMapping("/{id}/loyalty-points")
    public Integer getLoyaltyPoints(@PathVariable String id) {
        return service.getLoyaltyPoints(id);
    }

}
