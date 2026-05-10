package com.carcaddy.service;

import com.carcaddy.entity.Customer;
import com.carcaddy.dto.CustomerDTO;

import java.util.List;

public interface ICustomerService {

    Customer addCustomer(CustomerDTO dto);

    Customer updateCustomer(String customerId, CustomerDTO dto);

    Customer updateContact(String customerId, String contact);

    List<Customer> getAllCustomers();

    Customer getCustomerById(String customerId);

    List<Customer> getCustomersByName(String name);

    Customer blacklistCustomer(String customerId);

    Integer getLoyaltyPoints(String customerId);
}
