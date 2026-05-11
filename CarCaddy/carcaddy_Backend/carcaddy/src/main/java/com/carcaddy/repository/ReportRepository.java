package com.carcaddy.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface ReportRepository extends Repository<Object, Long> {

    @Query(value = """
        SELECT 
        (SELECT COUNT(*) FROM employee) as totalEmployees,
        (SELECT COUNT(*) FROM car) as totalCars,
        (SELECT COUNT(*) FROM customer) as totalCustomers,
        (SELECT COUNT(*) FROM booking) as totalBookings,
        (SELECT COUNT(*) FROM maintenance) as totalMaintenance
        """, nativeQuery = true)
    Object getDashboardStats();
}