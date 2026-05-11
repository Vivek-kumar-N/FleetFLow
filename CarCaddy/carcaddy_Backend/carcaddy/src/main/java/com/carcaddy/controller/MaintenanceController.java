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
@RequestMapping("/maintenance")
@Slf4j
public class MaintenanceController {

    private final IMaintenanceService service;

    public MaintenanceController(IMaintenanceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MaintenanceDto> add(
            @Valid @RequestBody MaintenanceDto dto) {

        log.info("API: Add maintenance for car {}", dto.getRegistrationNumber());

        return ResponseEntity.status(201).body(service.addMaintenance(dto));
    }

    @PostMapping("/routine")
    public ResponseEntity<MaintenanceDto> routine(
            @Valid @RequestBody MaintenanceDto dto) {

        log.info("API: Routine maintenance for car {}", dto.getRegistrationNumber());

        return ResponseEntity.status(201).body(service.scheduleRoutineMaintenance(dto));
    }

    @PostMapping("/emergency")
    public ResponseEntity<MaintenanceDto> emergency(
            @Valid @RequestBody MaintenanceDto dto) {

        log.info("API: Emergency maintenance for car {}", dto.getRegistrationNumber());

        return ResponseEntity.status(201).body(service.addEmergencyMaintenance(dto));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceDto>> getAll() {

        log.info("API: Fetch all maintenance records");

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/car/{regNumber}")
    public ResponseEntity<List<MaintenanceDto>> getByReg(
            @PathVariable String regNumber) {

        log.info("API: Fetch maintenance for car {}", regNumber);

        return ResponseEntity.ok(service.getByRegNumber(regNumber));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MaintenanceDto>> upcoming() {

        log.info("API: Fetch upcoming maintenance");

        return ResponseEntity.ok(service.getUpcomingMaintenance());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<MaintenanceDto>> overdue() {

        log.info("API: Fetch overdue maintenance");

        return ResponseEntity.ok(service.getOverdueMaintenance());
    }

    @GetMapping("/cost/{regNumber}")
    public ResponseEntity<Double> cost(
            @PathVariable String regNumber) {

        log.info("API: Fetch maintenance cost for car {}", regNumber);

        return ResponseEntity.ok(service.getTotalCostByCar(regNumber));
    }

    @GetMapping("/range")
    public ResponseEntity<List<MaintenanceDto>> range(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        log.info("API: Fetch maintenance between {} and {}", start, end);

        return ResponseEntity.ok(service.getByDateRange(start, end));
    }
}
