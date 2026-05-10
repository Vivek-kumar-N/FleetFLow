package com.carcaddy.service.impl;

import com.carcaddy.entity.Employee;
import com.carcaddy.repository.EmployeeRespository;
import com.carcaddy.service.EmailService;
import com.carcaddy.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRespository employeeRepository;
    private final EmailService emailService;

    public EmployeeServiceImpl(EmployeeRespository employeeRepository,
                               EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }

    // ✅ FIXED: Add employee without overriding valid incoming values
    @Override
    public Employee addEmployee(Employee employee) {

        // Set created time only if not provided
        if (employee.getCreatedAt() == null) {
            employee.setCreatedAt(LocalDateTime.now());
        }

        // Default accountActive = true only if not explicitly set
        if (employee.getAccountActive() == null) {
            employee.setAccountActive(true);
        }

        // Default firstLogin = true only if not explicitly set
        if (employee.getFirstLogin() == null) {
            employee.setFirstLogin(true);
        }

        // Default password generation only if not provided
        if (employee.getPassword() == null || employee.getPassword().isBlank()) {
            String defaultPassword =
                    employee.getEmployeeName().substring(0, 4).toLowerCase()
                            + employee.getDateOfBirth().getYear();
            employee.setPassword(defaultPassword);
        }

        // ✅ Auto‑deactivate immediately if expiry date is already past
        if (employee.getExpiryDate() != null &&
            employee.getExpiryDate().isBefore(LocalDate.now())) {

            employee.setAccountActive(false);
            employee.setFirstLogin(false);
        }

        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateContactNumber(Long employeeId, String contactNumber) {
        Employee employee = getEmployeeById(employeeId);
        employee.setContactNumber(contactNumber);
        return employeeRepository.save(employee);
    }

    @Override
    public void changePasswordOnFirstLogin(String emailId, String newPassword) {

        Employee employee = employeeRepository.findByEmailId(emailId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));

        if (!employee.getFirstLogin()) {
            throw new RuntimeException("Password already changed");
        }

        employee.setPassword(newPassword);
        employee.setFirstLogin(false);
        employeeRepository.save(employee);
    }

    @Override
    public void deleteEmployee(Long employeeId) {
        Employee employee = getEmployeeById(employeeId);
        employeeRepository.delete(employee);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));
    }

    @Override
    public List<Employee> getEmployeesByDesignation(String designation) {
        return employeeRepository.findByDesignation(designation);
    }

    @Override
    public Employee setExpiryDate(Long employeeId, LocalDate expiryDate) {
        Employee employee = getEmployeeById(employeeId);
        employee.setExpiryDate(expiryDate);

        // Optional: auto‑deactivate if expiry date set to past
        if (expiryDate.isBefore(LocalDate.now())) {
            employee.setAccountActive(false);
            employee.setFirstLogin(false);
        }

        return employeeRepository.save(employee);
    }

    @Override
    public void autoDeactivateExpiredEmployees() {

        List<Employee> expiredEmployees =
                employeeRepository.findByExpiryDateBeforeAndAccountActive(
                        LocalDate.now(), true
                );

        for (Employee employee : expiredEmployees) {
            employee.setAccountActive(false);
            employeeRepository.save(employee);

            emailService.sendAccountDeactivationEmail(
                    employee.getEmailId(),
                    employee.getEmployeeName()
            );
        }
    }
}