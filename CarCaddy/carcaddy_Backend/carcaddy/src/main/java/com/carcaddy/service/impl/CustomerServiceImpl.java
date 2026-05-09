package com.carcaddy.service.impl;

import com.carcaddy.dto.CustomerDTO;
import com.carcaddy.entity.Customer;
import com.carcaddy.exception.InvalidEntityException;
import com.carcaddy.repository.CustomerRepository;
import com.carcaddy.service.ICustomerService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository repository;
    private static final Logger log =
            LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Override
    public Customer addCustomer(CustomerDTO dto) {
        log.info("Adding new customer");

        // REQUIRED by document: uniqueness validation
        repository.findByEmailId(dto.getEmailId())
                .ifPresent(c -> {
                    throw new InvalidEntityException(
                            "Customer with email " + dto.getEmailId() + " already exists");
                });

        repository.findByDrivingLicense(dto.getDrivingLicense())
                .ifPresent(c -> {
                    throw new InvalidEntityException(
                            "Customer with driving license " + dto.getDrivingLicense() + " already exists");
                });

        Customer customer = new Customer();

        // String customerId (matches DB + Booking FK)
        customer.setCustomerId("CUST-" + UUID.randomUUID());

        customer.setCustomerName(dto.getCustomerName());
        customer.setContactNumber(dto.getContactNumber());
        customer.setDrivingLicense(dto.getDrivingLicense());
        customer.setOccupation(dto.getOccupation());
        customer.setAddress(dto.getAddress());
        customer.setEmailId(dto.getEmailId());
        customer.setBlacklisted(false);
        customer.setLoyaltyPoints(0);

        return repository.save(customer);
    }

    @Override
    public Customer updateCustomer(String id, CustomerDTO dto) {
        Customer customer = repository.findById(id)
                .orElseThrow(() ->
                        new InvalidEntityException("Customer ID " + id + " not found"));

        customer.setCustomerName(dto.getCustomerName());
        customer.setOccupation(dto.getOccupation());
        customer.setAddress(dto.getAddress());
        customer.setEmailId(dto.getEmailId());

        return repository.save(customer);
    }

    @Override
    public Customer updateContact(String id, String contact) {
        Customer customer = repository.findById(id)
                .orElseThrow(() ->
                        new InvalidEntityException("Customer ID " + id + " not found"));

        customer.setContactNumber(contact);
        return repository.save(customer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    @Override
    public Customer getCustomerById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new InvalidEntityException("Customer ID " + id + " not found"));
    }

    @Override
    public List<Customer> getCustomersByName(String name) {
        return repository.findByCustomerNameContainingIgnoreCase(name);
    }

    @Override
    public Customer blacklistCustomer(String id) {
        Customer customer = getCustomerById(id);
        customer.setBlacklisted(true);
        return repository.save(customer);
    }
}
