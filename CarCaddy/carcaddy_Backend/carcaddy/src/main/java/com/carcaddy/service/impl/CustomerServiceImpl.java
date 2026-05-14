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

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository repository;
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    // ---------------- DTO <-> Entity Mapping Methods----------------

    
     //Converts CustomerDTO to Customer entity (does not set system-controlled fields like customerId).
    
    private Customer dtoToEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setCustomerName(dto.getCustomerName());
        customer.setContactNumber(dto.getContactNumber());
        customer.setDrivingLicense(dto.getDrivingLicense());
        customer.setOccupation(dto.getOccupation());
        customer.setAddress(dto.getAddress());
        customer.setEmailId(dto.getEmailId());
        return customer;
    }

    
    // Converts Customer entity to CustomerDTO.
     
    private CustomerDTO entityToDto(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerName(customer.getCustomerName());
        dto.setContactNumber(customer.getContactNumber());
        dto.setDrivingLicense(customer.getDrivingLicense());
        dto.setOccupation(customer.getOccupation());
        dto.setAddress(customer.getAddress());
        dto.setEmailId(customer.getEmailId());
        return dto;
    }

    
     // Applies incoming DTO fields onto an existing Customer entity.
     // (Useful for update operations to keep the entity ID intact.)
     
    private void applyDtoToExistingEntity(Customer existing, CustomerDTO dto) {
        existing.setCustomerName(dto.getCustomerName());
        existing.setContactNumber(dto.getContactNumber());
        existing.setDrivingLicense(dto.getDrivingLicense());
        existing.setOccupation(dto.getOccupation());
        existing.setAddress(dto.getAddress());
        existing.setEmailId(dto.getEmailId());
    }

    // ---------------- Service Methods with Logging----------------

    @Override
    public Customer addCustomer(CustomerDTO dto) {
        log.info("CustomerServiceImpl.addCustomer() called | emailId={}, drivingLicense={}",
                dto.getEmailId(), dto.getDrivingLicense());

        // uniqueness validation
        repository.findByEmailId(dto.getEmailId()).ifPresent(c -> {
            log.warn("Add customer blocked: email already exists | emailId={}", dto.getEmailId());
            throw new InvalidEntityException("Customer with email " + dto.getEmailId() + " already exists");
        });

        repository.findByDrivingLicense(dto.getDrivingLicense()).ifPresent(c -> {
            log.warn("Add customer blocked: driving license already exists | drivingLicense={}",
                    dto.getDrivingLicense());
            throw new InvalidEntityException(
                    "Customer with driving license " + dto.getDrivingLicense() + " already exists");
        });

        // Generate sequential customerId in format CUST-001
        long count = repository.getCustomerCount() + 1;
        String customerId = String.format("CUST-%03d", count);

        // Use mapper method (DTO -> Entity)
        Customer customer = dtoToEntity(dto);
        customer.setCustomerId(customerId);

        // system defaults
        customer.setBlacklisted(false);
        customer.setLoyaltyPoints(0);

        Customer saved = repository.save(customer);
        log.info("Customer created successfully | customerId={}", saved.getCustomerId());

        // entityToDto() implemented; can be used later for DTO-based responses if
        // needed
        return saved;
    }

    @Override
    public Customer updateCustomer(String id, CustomerDTO dto) {
        log.info("CustomerServiceImpl.updateCustomer() called | customerId={}", id);

        Customer customer = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update customer failed: customer not found | customerId={}", id);
                    return new InvalidEntityException("Customer ID " + id + " not found");
                });

        // Keep ID, update other fields (use helper)
        // If you don't want driving license/contact updates here, you can remove those
        // lines from applyDtoToExistingEntity.
        applyDtoToExistingEntity(customer, dto);

        Customer updated = repository.save(customer);
        log.info("Customer updated successfully | customerId={}", updated.getCustomerId());
        return updated;
    }

    @Override
    public Customer updateContact(String id, String contact) {
        log.info("CustomerServiceImpl.updateContact() called | customerId={}, newContact={}", id, contact);

        Customer customer = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update contact failed: customer not found | customerId={}", id);
                    return new InvalidEntityException("Customer ID " + id + " not found");
                });

        customer.setContactNumber(contact);
        Customer updated = repository.save(customer);

        log.info("Customer contact updated successfully | customerId={}", updated.getCustomerId());
        return updated;
    }

    @Override
    public List<Customer> getAllCustomers() {
        log.info("CustomerServiceImpl.getAllCustomers() called");
        List<Customer> customers = repository.findAll();
        log.info("Total customers fetched={}", customers.size());
        return customers;
    }

    @Override
    public Customer getCustomerById(String id) {
        log.info("CustomerServiceImpl.getCustomerById() called | customerId={}", id);

        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Get customer failed: customer not found | customerId={}", id);
                    return new InvalidEntityException("Customer ID " + id + " not found");
                });
    }

    @Override
    public List<Customer> getCustomersByName(String name) {
        log.info("CustomerServiceImpl.getCustomersByName() called | name={}", name);

        List<Customer> customers = repository.findByCustomerNameContainingIgnoreCase(name);
        log.info("Customers matched by name='{}' => count={}", name, customers.size());
        return customers;
    }

    @Override
    public Customer blacklistCustomer(String id) {
        log.info("CustomerServiceImpl.blacklistCustomer() called | customerId={}", id);

        Customer customer = getCustomerById(id);
        customer.setBlacklisted(true);

        Customer updated = repository.save(customer);
        log.info("Customer blacklisted successfully | customerId={}", updated.getCustomerId());
        return updated;
    }

    @Override
    public Integer getLoyaltyPoints(String customerId) {
        log.info("CustomerServiceImpl.getLoyaltyPoints() called | customerId={}", customerId);

        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Get loyalty points failed: customer not found | customerId={}", customerId);
                    return new InvalidEntityException("Customer ID " + customerId + " not found");
                });

        Integer points = customer.getLoyaltyPoints();
        log.info("Loyalty points fetched | customerId={}, points={}", customerId, points);
        return points;
    }


    @Override
public double calculateLoyaltyDiscount(String customerId) {
    log.info("CustomerServiceImpl.calculateLoyaltyDiscount() called | customerId={}", customerId);

    Customer customer = repository.findById(customerId)
            .orElseThrow(() -> {
                log.warn("Calculate discount failed: customer not found | customerId={}", customerId);
                return new InvalidEntityException("Customer ID " + customerId + " not found");
            });

    int points = customer.getLoyaltyPoints();
    double discount = points >= 100 ? 0.05 : 0.0;

    log.info("Loyalty discount calculated | customerId={}, points={}, discount={}",
            customerId, points, discount);

    return discount;
}


@Override
public boolean isEligibleForFreeRental(String customerId) {
    log.info("CustomerServiceImpl.isEligibleForFreeRental() called | customerId={}", customerId);

    Customer customer = repository.findById(customerId)
            .orElseThrow(() -> {
                log.warn("Free rental check failed: customer not found | customerId={}", customerId);
                return new InvalidEntityException("Customer ID " + customerId + " not found");
            });

    boolean eligible = customer.getLoyaltyPoints() >= 500;

    log.info("Free rental eligibility result | customerId={}, eligible={}",
            customerId, eligible);

    return eligible;
}


@Override
public List<Customer> getCustomersWithMaximumBookings() {
    log.info("CustomerServiceImpl.getCustomersWithMaximumBookings() called");

    List<String> customerIds = repository.findCustomerIdsWithMaximumBookings();
    List<Customer> customers = repository.findAllById(customerIds);

    log.info("Customers with maximum bookings fetched | count={}", customers.size());

    return customers;
}

    @Override
    public void deleteCustomer(String customerId) {
        log.info("CustomerServiceImpl.deleteCustomer() called | customerId={}", customerId);

        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Delete customer failed: customer not found | customerId={}", customerId);
                    return new InvalidEntityException("Customer ID " + customerId + " not found");
                });

        repository.delete(customer);
        log.info("Customer deleted successfully | customerId={}", customerId);
    }

    @Override
    public Customer getCustomerByUsername(String username) {
        log.info("CustomerServiceImpl.getCustomerByUsername() called | username={}", username);

        return repository.findByAppUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Get customer by username failed: not found | username={}", username);
                    return new InvalidEntityException("No customer profile linked to username: " + username);
                });
    }

}