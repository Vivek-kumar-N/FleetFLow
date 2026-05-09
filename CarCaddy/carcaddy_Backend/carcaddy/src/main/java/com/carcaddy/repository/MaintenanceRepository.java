package com.carcaddy.repository;

import com.carcaddy.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    List<Maintenance> findByRegistrationNumber(String regNumber);

    List<Maintenance> findByMaintenanceType(MaintenanceType type);

    List<Maintenance> findByStatus(MaintenanceStatus status);
}
