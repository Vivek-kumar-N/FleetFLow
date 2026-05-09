package com.carcaddy.controller;

import com.carcaddy.dto.MaintenanceDto;
import com.carcaddy.entity.*;
import com.carcaddy.service.IMaintenanceService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final IMaintenanceService service;

    public MaintenanceController(IMaintenanceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MaintenanceDto> add(@Valid @RequestBody MaintenanceDto dto) {
        return ResponseEntity.status(201).body(service.addMaintenance(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/car/{regNumber}")
    public ResponseEntity<List<MaintenanceDto>> getByRegNumber(@PathVariable String regNumber) {
        return ResponseEntity.ok(service.getByRegNumber(regNumber));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<MaintenanceDto>> getByType(@PathVariable MaintenanceType type) {
        return ResponseEntity.ok(service.getByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MaintenanceDto>> getByStatus(@PathVariable MaintenanceStatus status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MaintenanceDto> updateStatus(
            @PathVariable Long id,
            @RequestParam MaintenanceStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
