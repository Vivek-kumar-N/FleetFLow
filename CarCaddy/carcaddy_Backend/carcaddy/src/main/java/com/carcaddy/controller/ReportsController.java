
package com.carcaddy.controller;

import com.carcaddy.entity.Customer;
import com.carcaddy.service.IReportsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportsController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
    
    @Autowired
    private IReportsService reportsService;

    @GetMapping("/customers/max-bookings")
    public ResponseEntity<List<Map<String, Object>>> getCustomersWithMaxBookings() {
        logger.info("Generating customers with maximum bookings report");
        List<Customer> customers = reportsService.getCustomersWithMaxBookings();
        // Return with bookingCount format for frontend compatibility
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Customer c : customers) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("customerId", c.getCustomerId());
            item.put("customerName", c.getCustomerName());
            item.put("emailId", c.getEmailId());
            item.put("loyaltyPoints", c.getLoyaltyPoints());
            item.put("blacklisted", c.getBlacklisted());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cars/minimal-bookings")
    public ResponseEntity<Map<String, Long>> getCarsWithMinimalBookings() {
        logger.info("Generating cars with minimal bookings report");
        Map<String, Long> report = reportsService.getCarsWithMinimalBookings();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/income/by-model")
    public ResponseEntity<Map<String, BigDecimal>> getIncomeByCarModel() {
        logger.info("Generating income by car model report");
        Map<String, BigDecimal> report = reportsService.getIncomeByCarModel();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/period")
    public ResponseEntity<BigDecimal> getRevenueForPeriod(@RequestParam String startDate, 
                                                         @RequestParam String endDate) {
        logger.info("Calculating revenue for period {} to {}", startDate, endDate);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        BigDecimal revenue = reportsService.getRevenueForPeriod(start, end);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/maintenance/cost-by-model")
    public ResponseEntity<Map<String, BigDecimal>> getMaintenanceCostByCarModel() {
        logger.info("Generating maintenance cost by car model report");
        Map<String, BigDecimal> report = reportsService.getMaintenanceCostByCarModel();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/maintenance/cost-by-period")
    public ResponseEntity<Map<String, BigDecimal>> getMaintenanceCostByPeriod(@RequestParam String startDate,
                                                                             @RequestParam String endDate) {
        logger.info("Generating maintenance cost by period report");
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        Map<String, BigDecimal> report = reportsService.getMaintenanceCostByPeriod(start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        logger.info("Generating dashboard statistics");
        Map<String, Object> stats = reportsService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/car-utilization")
    public ResponseEntity<Map<String, Long>> getCarUtilizationReport() {
        logger.info("Generating car utilization report");
        Map<String, Long> report = reportsService.getCarUtilizationReport();
        return ResponseEntity.ok(report);
    }

    // Alias for SRS compatibility
    @GetMapping("/cars/utilization")
    public ResponseEntity<List<Map<String, Object>>> getCarUtilizationList() {
        logger.info("Generating car utilization list report");
        Map<String, Long> report = reportsService.getCarUtilizationReport();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<String, Long> entry : report.entrySet()) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("registrationNumber", entry.getKey());
            item.put("bookingCount", entry.getValue());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer-loyalty")
    public ResponseEntity<Map<String, Object>> getCustomerLoyaltyAnalytics() {
        logger.info("Generating customer loyalty analytics");
        Map<String, Object> analytics = reportsService.getCustomerLoyaltyAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/profitability")
    public ResponseEntity<Map<String, BigDecimal>> getProfitabilityReport() {
        logger.info("Generating profitability report");
        Map<String, BigDecimal> report = reportsService.getProfitabilityReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/fleet-health")
    public ResponseEntity<Map<String, Object>> getFleetHealthReport() {
        logger.info("Generating fleet health report");
        Map<String, Object> report = reportsService.getFleetHealthReport();
        return ResponseEntity.ok(report);
    }
}
