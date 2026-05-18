
package com.carcaddy.service;

import com.carcaddy.entity.Employee;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeService {

    // Add employee with auto‑generated password
    Employee addEmployee(Employee employee);

    // Update employee contact number
    Employee updateContactNumber(Long employeeId, String contactNumber);

    // Change password on first login
    void changePasswordOnFirstLogin(String emailId, String newPassword);

    // Delete employee account
    void deleteEmployee(Long employeeId);

    // View all employees
    List<Employee> getAllEmployees();

    // View employee by ID
    Employee getEmployeeById(Long employeeId);

    // View employees by designation
    List<Employee> getEmployeesByDesignation(String designation);

    // Set expiry date for temporary employee
    Employee setExpiryDate(Long employeeId, LocalDate expiryDate);

    // Auto‑deactivate expired temporary employees and return the list
    List<Employee> autoDeactivateExpiredEmployees();
}



