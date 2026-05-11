package com.carcaddy.repository;

import com.carcaddy.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    @Query("SELECT m FROM Maintenance m WHERE m.car.registrationNumber = :reg")
    List<Maintenance> findByRegistrationNumber(@Param("reg") String regNumber);

    List<Maintenance> findByMaintenanceType(MaintenanceType type);

    List<Maintenance> findByStatus(MaintenanceStatus status);

    @Query("SELECT SUM(m.cost) FROM Maintenance m WHERE m.car.registrationNumber = :reg")
    Double getMaintenanceCostByCar(@Param("reg") String regNumber);

    List<Maintenance> findByScheduledDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT m FROM Maintenance m WHERE m.scheduledDate >= CURRENT_DATE AND m.status = 'SCHEDULED'")
    List<Maintenance> findUpcoming();

    @Query("SELECT m FROM Maintenance m WHERE m.scheduledDate < CURRENT_DATE AND m.status != 'COMPLETED'")
    List<Maintenance> findOverdue();
}
