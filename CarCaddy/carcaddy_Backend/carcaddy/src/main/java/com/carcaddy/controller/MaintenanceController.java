package com.carcaddy.controller;

import com.carcaddy.dto.MaintenanceDto;
import com.carcaddy.entity.*;
import com.carcaddy.service.IMaintenanceService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@Slf4j
@CrossOrigin(origins = "*")
public class MaintenanceController {

    private final IMaintenanceService service;

    public MaintenanceController(IMaintenanceService service) {
        this.service = service;
    }

    //Add Maintenance
    @PostMapping
    public ResponseEntity<MaintenanceDto> add(
            @Valid @RequestBody MaintenanceDto dto) {

        log.info("API: Add maintenance for car {}", dto.getRegistrationNumber());

        return ResponseEntity.status(201).body(service.addMaintenance(dto));
    }

    //Routine Maintenance
    @PostMapping("/routine")
    public ResponseEntity<MaintenanceDto> routine(
            @Valid @RequestBody MaintenanceDto dto) {

        log.info("API: Routine maintenance for car {}", dto.getRegistrationNumber());

        return ResponseEntity.status(201).body(service.scheduleRoutineMaintenance(dto));
    }

    //Emergency Maintenance
    @PostMapping("/emergency")
    public ResponseEntity<MaintenanceDto> emergency(
            @Valid @RequestBody MaintenanceDto dto) {

        log.info("API: Emergency maintenance for car {}", dto.getRegistrationNumber());

        return ResponseEntity.status(201).body(service.addEmergencyMaintenance(dto));
    }


    

    // Update Status
    @PatchMapping("/{id}/status")
    public ResponseEntity<MaintenanceDto> updateStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {

        log.info("API: Updating maintenance ID {} status", id);

        String statusStr = (String) body.get("status");
        MaintenanceStatus status = MaintenanceStatus.valueOf(statusStr);

        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // Update Status via query param (backward compat)
    @PutMapping("/{id}/status")
    public ResponseEntity<MaintenanceDto> updateStatusParam(
            @PathVariable Long id,
            @RequestParam MaintenanceStatus status) {

        log.info("API: Updating maintenance ID {} to status {}", id, status);

        return ResponseEntity.ok(service.updateStatus(id, status));
    }



    //Get all
    @GetMapping
    public ResponseEntity<List<MaintenanceDto>> getAll() {

        log.info("API: Fetch all maintenance records");

        return ResponseEntity.ok(service.getAll());
    }

    //Get by car
    @GetMapping("/car/{regNumber}")
    public ResponseEntity<List<MaintenanceDto>> getByReg(
            @PathVariable String regNumber) {

        log.info("API: Fetch maintenance for car {}", regNumber);

        return ResponseEntity.ok(service.getByRegNumber(regNumber));
    }



    //  Get By Type
    @GetMapping("/type")
    public ResponseEntity<List<MaintenanceDto>> getByType(
            @RequestParam MaintenanceType type) {

        log.info("API: Fetching maintenance records by type {}", type);

        return ResponseEntity.ok(service.getByType(type));
    }



    //  Get By Status
    @GetMapping("/status")
    public ResponseEntity<List<MaintenanceDto>> getByStatus(
            @RequestParam MaintenanceStatus status) {

        log.info("API: Fetching maintenance records by status {}", status);

        return ResponseEntity.ok(service.getByStatus(status));
    }


    //Upcoming
    @GetMapping("/upcoming")
    public ResponseEntity<List<MaintenanceDto>> upcoming() {

        log.info("API: Fetch upcoming maintenance");

        return ResponseEntity.ok(service.getUpcomingMaintenance());
    }

    //Overdue
    @GetMapping("/overdue")
    public ResponseEntity<List<MaintenanceDto>> overdue() {

        log.info("API: Fetch overdue maintenance");

        return ResponseEntity.ok(service.getOverdueMaintenance());
    }

    //Total cost
    @GetMapping("/cost/{regNumber}")
    public ResponseEntity<Double> cost(
            @PathVariable String regNumber) {

        log.info("API: Fetch maintenance cost for car {}", regNumber);

        return ResponseEntity.ok(service.getTotalCostByCar(regNumber));
    }

    //Date range
    @GetMapping("/range")
    public ResponseEntity<List<MaintenanceDto>> range(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        log.info("API: Fetch maintenance between {} and {}", start, end);

        return ResponseEntity.ok(service.getByDateRange(start, end));
    }


    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceDto> getById(@PathVariable Long id) {
    
        log.info("API: Fetching maintenance by ID {}", id);
    
        return ResponseEntity.ok(service.getById(id));
    }   
    


    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
    
        log.info("API: Deleting maintenance with ID {}", id);
    
        service.delete(id);
    
        return ResponseEntity.ok("Maintenance deleted successfully");
    }
    
}
