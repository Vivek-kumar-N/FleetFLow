package com.carcaddy.controller;

import com.carcaddy.dto.CustomerDTO;
import com.carcaddy.entity.Customer;
import com.carcaddy.service.ICustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    // Add new customer
    @PostMapping
    public ResponseEntity<Customer> addCustomer(@Valid @RequestBody CustomerDTO dto) {
        log.info("POST /api/customers called | emailId={}, drivingLicense={}", dto.getEmailId(), dto.getDrivingLicense());
        Customer created = service.addCustomer(dto);
        log.info("Customer created | customerId={}", created.getCustomerId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Update customer details
    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable String id, @Valid @RequestBody CustomerDTO dto) {
        log.info("PUT /api/customers/{} called", id);
        Customer updated = service.updateCustomer(id, dto);
        log.info("Customer updated | customerId={}", updated.getCustomerId());
        return updated;
    }

    // Update contact number
    @PatchMapping("/{id}/contact/{contact}")
    public Customer updateContact(@PathVariable String id, @PathVariable String contact) {
        log.info("PATCH /api/customers/{}/contact/{} called", id, contact);
        Customer updated = service.updateContact(id, contact);
        log.info("Customer contact updated | customerId={}", updated.getCustomerId());
        return updated;
    }

    // Get all customers
    @GetMapping
    public List<Customer> getAll() {
        log.info("GET /api/customers called");
        List<Customer> customers = service.getAllCustomers();
        log.info("Customers returned count={}", customers.size());
        return customers;
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public Customer getById(@PathVariable String id) {
        log.info("GET /api/customers/{} called", id);
        return service.getCustomerById(id);
    }

    // Search customers by name
    @GetMapping("/search/{name}")
    public List<Customer> getByName(@PathVariable String name) {
        log.info("GET /api/customers/search/{} called", name);
        List<Customer> customers = service.getCustomersByName(name);
        log.info("Search result count={} for name={}", customers.size(), name);
        return customers;
    }

    // Blacklist customer
    @PatchMapping("/{id}/blacklist")
    public Customer blacklist(@PathVariable String id) {
        log.info("PATCH /api/customers/{}/blacklist called", id);
        Customer updated = service.blacklistCustomer(id);
        log.info("Customer blacklisted | customerId={}", updated.getCustomerId());
        return updated;
    }

    // Get loyalty points
    @GetMapping("/{id}/loyalty-points")
    public Integer getLoyaltyPoints(@PathVariable String id) {
        log.info("GET /api/customers/{}/loyalty-points called", id);
        Integer points = service.getLoyaltyPoints(id);
        log.info("Loyalty points returned | customerId={}, points={}", id, points);
        return points;
    }
}