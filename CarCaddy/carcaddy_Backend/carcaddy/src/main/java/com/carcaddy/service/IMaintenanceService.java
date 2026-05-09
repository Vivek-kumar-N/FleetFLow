package com.carcaddy.service;

import com.carcaddy.dto.MaintenanceDto;
import com.carcaddy.entity.*;

import java.util.List;

public interface IMaintenanceService {

    MaintenanceDto addMaintenance(MaintenanceDto dto);

    MaintenanceDto updateStatus(Long id, MaintenanceStatus status);

    MaintenanceDto getById(Long id);

    List<MaintenanceDto> getAll();

    List<MaintenanceDto> getByRegNumber(String regNumber);

    List<MaintenanceDto> getByType(MaintenanceType type);

    List<MaintenanceDto> getByStatus(MaintenanceStatus status);

    void delete(Long id);
}