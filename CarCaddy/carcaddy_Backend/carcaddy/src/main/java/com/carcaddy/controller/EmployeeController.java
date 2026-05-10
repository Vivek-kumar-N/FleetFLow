package com.carcaddy.controller;

import com.carcaddy.entity.Employee;
import com.carcaddy.service.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /* =========================================================
       1. Add new employee with auto‑generated default password
       ========================================================= */
  
@PostMapping
public ResponseEntity<Employee> addEmployee(
        @Valid @RequestBody Employee employee) {
    return new ResponseEntity<>(
        employeeService.addEmployee(employee),
        HttpStatus.CREATED
    );
}

    /* =========================================================
       2. Update employee contact number
       ========================================================= */
    @PutMapping("/{id}/contact")
    public ResponseEntity<Employee> updateContactNumber(
            @PathVariable("id") Long employeeId,
            @RequestParam String contactNumber) {

        Employee updatedEmployee =
                employeeService.updateContactNumber(employeeId, contactNumber);

        return ResponseEntity.ok(updatedEmployee);
    }

    /* =========================================================
       3. Change password on first login
       ========================================================= */
    @PutMapping("/change-password")
    public ResponseEntity<String> changePasswordOnFirstLogin(
            @RequestParam String emailId,
            @RequestParam String newPassword) {

        employeeService.changePasswordOnFirstLogin(emailId, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    /* =========================================================
       4. Delete employee account
       ========================================================= */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable("id") Long employeeId) {

        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    /* =========================================================
       5. View all employees
       ========================================================= */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /* =========================================================
       6. View employee by ID
       ========================================================= */
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable("id") Long employeeId) {

        return ResponseEntity.ok(employeeService.getEmployeeById(employeeId));
    }

    /* =========================================================
       7. View employees by designation
       ========================================================= */
    @GetMapping("/designation/{designation}")
    public ResponseEntity<List<Employee>> getEmployeesByDesignation(
            @PathVariable String designation) {

        return ResponseEntity.ok(
                employeeService.getEmployeesByDesignation(designation)
        );
    }

    /* =========================================================
       8. Set expiry date for temporary employees
       ========================================================= */
    @PutMapping("/{id}/expiry")
    public ResponseEntity<Employee> setExpiryDate(
            @PathVariable("id") Long employeeId,
            @RequestParam LocalDate expiryDate) {

        Employee employee =
                employeeService.setExpiryDate(employeeId, expiryDate);

        return ResponseEntity.ok(employee);
    }

    /* =========================================================
       9. Auto‑deactivate expired temporary employees
       (Can be called manually or via scheduler)
       ========================================================= */
    @PutMapping("/auto-deactivate")
    public ResponseEntity<String> autoDeactivateExpiredEmployees() {

        employeeService.autoDeactivateExpiredEmployees();
        return ResponseEntity.ok(
                "Expired temporary employee accounts deactivated successfully"
        );
    }
}