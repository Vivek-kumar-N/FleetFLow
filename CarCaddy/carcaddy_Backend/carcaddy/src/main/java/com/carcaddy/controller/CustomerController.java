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

    // ---------------- ADD CUSTOMER ----------------
    @PostMapping
    public ResponseEntity<Customer> addCustomer(@Valid @RequestBody CustomerDTO dto) {
        log.info("POST /api/customers | Creating customer | emailId={}, drivingLicense={}",
                dto.getEmailId(), dto.getDrivingLicense());

        Customer customer = service.addCustomer(dto);

        log.info("Customer created successfully | customerId={}", customer.getCustomerId());
        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }

    // ---------------- UPDATE CUSTOMER ----------------
    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable String id,
                                   @Valid @RequestBody CustomerDTO dto) {

        log.info("PUT /api/customers/{} | Updating customer details", id);
        Customer updated = service.updateCustomer(id, dto);
        log.info("Customer updated successfully | customerId={}", updated.getCustomerId());

        return updated;
    }

    // ---------------- UPDATE CONTACT NUMBER ----------------
    @PatchMapping("/{id}/contact/{contact}")
    public Customer updateContact(@PathVariable String id,
                                  @PathVariable String contact) {

        log.info("PATCH /api/customers/{}/contact/{} | Updating contact number", id, contact);
        Customer updated = service.updateContact(id, contact);
        log.info("Customer contact updated | customerId={}", updated.getCustomerId());

        return updated;
    }

    // ---------------- GET ALL CUSTOMERS ----------------
    @GetMapping
    public List<Customer> getAllCustomers() {
        log.info("GET /api/customers | Fetching all customers");

        List<Customer> customers = service.getAllCustomers();
        log.info("Total customers fetched={}", customers.size());

        return customers;
    }

    // ---------------- GET CUSTOMER BY ID ----------------
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable String id) {
        log.info("GET /api/customers/{} | Fetching customer by ID", id);
        return service.getCustomerById(id);
    }

    // ---------------- SEARCH CUSTOMER BY NAME ----------------
    @GetMapping("/search/{name}")
    public List<Customer> getCustomersByName(@PathVariable String name) {
        log.info("GET /api/customers/search/{} | Searching customers by name", name);

        List<Customer> customers = service.getCustomersByName(name);
        log.info("Customers found with name='{}' | count={}", name, customers.size());

        return customers;
    }

    // ---------------- BLACKLIST CUSTOMER ----------------
    @PatchMapping("/{id}/blacklist")
    public Customer blacklistCustomer(@PathVariable String id) {
        log.info("PATCH /api/customers/{}/blacklist | Blacklisting customer", id);

        Customer customer = service.blacklistCustomer(id);
        log.info("Customer blacklisted successfully | customerId={}", customer.getCustomerId());

        return customer;
    }

    // ---------------- GET LOYALTY POINTS ----------------
    @GetMapping("/{id}/loyalty-points")
    public Integer getLoyaltyPoints(@PathVariable String id) {
        log.info("GET /api/customers/{}/loyalty-points | Fetching loyalty points", id);

        Integer points = service.getLoyaltyPoints(id);
        log.info("Loyalty points fetched | customerId={}, points={}", id, points);

        return points;
    }

    // ---------------- GET LOYALTY DISCOUNT ----------------
    @GetMapping("/{id}/loyalty-discount")
    public double getLoyaltyDiscount(@PathVariable String id) {
        log.info("GET /api/customers/{}/loyalty-discount | Calculating loyalty discount", id);

        double discount = service.calculateLoyaltyDiscount(id);
        log.info("Loyalty discount calculated | customerId={}, discount={}", id, discount);

        return discount;
    }

    // ---------------- FREE RENTAL ELIGIBILITY ----------------
    @GetMapping("/{id}/free-rental-eligibility")
    public boolean isEligibleForFreeRental(@PathVariable String id) {
        log.info("GET /api/customers/{}/free-rental-eligibility | Checking eligibility", id);

        boolean eligible = service.isEligibleForFreeRental(id);
        log.info("Free rental eligibility | customerId={}, eligible={}", id, eligible);

        return eligible;
    }

    // ---------------- CUSTOMERS WITH MAX BOOKINGS ----------------
    @GetMapping("/max-bookings")
    public List<Customer> getCustomersWithMaxBookings() {
        log.info("GET /api/customers/max-bookings | Fetching customers with maximum bookings");

        List<Customer> customers = service.getCustomersWithMaximumBookings();
        log.info("Customers with maximum bookings fetched | count={}", customers.size());

        return customers;
    }

    // ---------------- DELETE CUSTOMER ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        log.info("DELETE /api/customers/{} | Deleting customer", id);

        service.deleteCustomer(id);
        log.info("Customer deleted successfully | customerId={}", id);

        return ResponseEntity.noContent().build();
    }
}