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

    /**
     * Find active (non-completed, non-cancelled) maintenance records for a car
     * whose scheduled date falls within the given booking date range.
     * A booking cannot be created if maintenance is scheduled during that period.
     */
    @Query("""
        SELECT m FROM Maintenance m
        WHERE m.car.registrationNumber = :reg
        AND m.status NOT IN ('COMPLETED', 'CANCELLED')
        AND m.scheduledDate >= :startDate
        AND m.scheduledDate <= :endDate
    """)
    List<Maintenance> findMaintenanceOverlapping(
            @Param("reg") String regNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
