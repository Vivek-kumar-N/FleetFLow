package com.carcaddy.service;

import com.carcaddy.dto.MaintenanceDto;
import com.carcaddy.entity.*;

import java.time.LocalDate;
import java.util.List;

public interface IMaintenanceService {

    MaintenanceDto addMaintenance(MaintenanceDto dto);

    MaintenanceDto scheduleRoutineMaintenance(MaintenanceDto dto);

    MaintenanceDto addEmergencyMaintenance(MaintenanceDto dto);

    MaintenanceDto updateStatus(Long id, MaintenanceStatus status);

    MaintenanceDto patchDetails(Long id, MaintenanceDto dto);

    MaintenanceDto getById(Long id);

    List<MaintenanceDto> getAll();

    List<MaintenanceDto> getByRegNumber(String regNumber);

    List<MaintenanceDto> getByType(MaintenanceType type);

    List<MaintenanceDto> getByStatus(MaintenanceStatus status);

    List<MaintenanceDto> getUpcomingMaintenance();

    List<MaintenanceDto> getOverdueMaintenance();

    double getTotalCostByCar(String regNumber);

    List<MaintenanceDto> getByDateRange(LocalDate start, LocalDate end);

    void delete(Long id);
}

