package com.carcaddy.service.impl;

import com.carcaddy.entity.Employee;
import com.carcaddy.exception.InvalidEntityException;
import com.carcaddy.repository.EmployeeRespository;
import com.carcaddy.service.EmailService;
import com.carcaddy.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRespository employeeRepository;
    private final EmailService emailService;

    public EmployeeServiceImpl(EmployeeRespository employeeRepository,
                               EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }

    @Override
    public Employee addEmployee(Employee employee) {

        logger.info("Attempting to add employee with email {}", employee.getEmailId());

        // ✅ Duplicate checks
        if (employeeRepository.existsByEmailId(employee.getEmailId())) {
            throw new InvalidEntityException(
                "Employee with email " + employee.getEmailId() + " already exists"
            );
        }

        if (employeeRepository.existsByContactNumber(employee.getContactNumber())) {
            throw new InvalidEntityException(
                "Employee with contact number " + employee.getContactNumber() + " already exists"
            );
        }

        if (employee.getCreatedAt() == null) {
            employee.setCreatedAt(LocalDateTime.now());
        }

        if (employee.getAccountActive() == null) {
            employee.setAccountActive(true);
        }

        if (employee.getFirstLogin() == null) {
            employee.setFirstLogin(true);
        }

        if (employee.getPassword() == null || employee.getPassword().isBlank()) {
            String defaultPassword =
                    employee.getAccountType().substring(0, 1).toUpperCase()
                            + employee.getDateOfBirth()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + employee.getEmployeeName().length();
            employee.setPassword(defaultPassword);
        }

        if (employee.getExpiryDate() != null &&
            employee.getExpiryDate().isBefore(LocalDate.now())) {
            employee.setAccountActive(false);
            employee.setFirstLogin(false);
        }

        logger.info("Employee added successfully");
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
                        new InvalidEntityException("Employee with email " + emailId + " not found")
                );

        if (!employee.getFirstLogin()) {
            throw new InvalidEntityException("Password already changed");
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
                        new InvalidEntityException(
                            "Employee Id " + employeeId + " is not found")
                );
    }

    @Override
    public List<Employee> getEmployeesByDesignation(String designation) {
        return employeeRepository.findByDesignation(designation);
    }

    @Override
    public Employee setExpiryDate(Long employeeId, LocalDate expiryDate) {
        Employee employee = getEmployeeById(employeeId);
        employee.setExpiryDate(expiryDate);

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