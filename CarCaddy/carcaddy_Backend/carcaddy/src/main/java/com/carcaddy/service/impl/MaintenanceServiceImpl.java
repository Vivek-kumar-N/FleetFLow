package com.carcaddy.service.impl;

import com.carcaddy.dto.MaintenanceDto;
import com.carcaddy.entity.*;
import com.carcaddy.exception.InvalidEntityException;
import com.carcaddy.repository.MaintenanceRepository;
import com.carcaddy.service.IMaintenanceService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MaintenanceServiceImpl implements IMaintenanceService {

    private final MaintenanceRepository repo;

    public MaintenanceServiceImpl(MaintenanceRepository repo) {
        this.repo = repo;
    }

    // DTO -> Entity
    private Maintenance convertToEntity(MaintenanceDto dto) {
        Maintenance m = new Maintenance();
        m.setMaintenanceId(dto.getMaintenanceId());
        m.setRegistrationNumber(dto.getRegistrationNumber());
        m.setMaintenanceType(dto.getMaintenanceType());
        m.setScheduledDate(dto.getScheduledDate());
        m.setCompletedDate(dto.getCompletedDate());
        m.setDescription(dto.getDescription());
        m.setCost(dto.getCost());
        m.setPerformedBy(dto.getPerformedBy());
        m.setStatus(dto.getStatus());
        return m;
    }

    // Entity -> DTO
    private MaintenanceDto convertToDTO(Maintenance m) {
        MaintenanceDto dto = new MaintenanceDto();
        dto.setMaintenanceId(m.getMaintenanceId());
        dto.setRegistrationNumber(m.getRegistrationNumber());
        dto.setMaintenanceType(m.getMaintenanceType());
        dto.setScheduledDate(m.getScheduledDate());
        dto.setCompletedDate(m.getCompletedDate());
        dto.setDescription(m.getDescription());
        dto.setCost(m.getCost());
        dto.setPerformedBy(m.getPerformedBy());
        dto.setStatus(m.getStatus());
        return dto;
    }

    @Override
    public MaintenanceDto addMaintenance(MaintenanceDto dto) {
        log.info("Adding maintenance record");
        return convertToDTO(repo.save(convertToEntity(dto)));
    }

    @Override
    public MaintenanceDto updateStatus(Long id, MaintenanceStatus status) {
        Maintenance m = repo.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Maintenance ID " + id + " not found"));

        m.setStatus(status);
        return convertToDTO(repo.save(m));
    }

    @Override
    public MaintenanceDto getById(Long id) {
        return repo.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new InvalidEntityException("Maintenance ID " + id + " not found"));
    }

    @Override
    public List<MaintenanceDto> getAll() {
        return repo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDto> getByRegNumber(String regNumber) {
        return repo.findByRegistrationNumber(regNumber)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDto> getByType(MaintenanceType type) {
        return repo.findByMaintenanceType(type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDto> getByStatus(MaintenanceStatus status) {
        return repo.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new InvalidEntityException("Maintenance ID " + id + " not found");
        }
        repo.deleteById(id);
    }
}