package com.carcaddy.controller;

import com.carcaddy.entity.Employee;
import com.carcaddy.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<Employee> addEmployee(
            @Valid @RequestBody Employee employee) {

        logger.info("Add employee request received");
        Employee savedEmployee = employeeService.addEmployee(employee);
        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/contact")
    public ResponseEntity<Employee> updateContactNumber(
            @PathVariable Long id,
            @RequestParam String contactNumber) {

        return ResponseEntity.ok(
                employeeService.updateContactNumber(id, contactNumber)
        );
    }

    @PatchMapping("/{id}/contact")
    public ResponseEntity<Employee> updateContactNumberPatch(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {

        String contactNumber = body.get("contactNumber");
        return ResponseEntity.ok(
                employeeService.updateContactNumber(id, contactNumber)
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String emailId,
            @RequestParam String newPassword) {

        employeeService.changePasswordOnFirstLogin(emailId, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public Employee getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @GetMapping("/designation/{designation}")
    public List<Employee> getEmployeesByDesignation(
            @PathVariable String designation) {
        return employeeService.getEmployeesByDesignation(designation);
    }

    @PutMapping("/{id}/expiry")
    public Employee setExpiryDate(
            @PathVariable Long id,
            @RequestParam LocalDate expiryDate) {
        return employeeService.setExpiryDate(id, expiryDate);
    }

    @PutMapping("/auto-deactivate")
    public ResponseEntity<List<Employee>> autoDeactivate() {
        List<Employee> deactivated = employeeService.autoDeactivateExpiredEmployees();
        return ResponseEntity.ok(deactivated);
    }
}