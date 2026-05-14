package com.carcaddy.service;

import com.carcaddy.entity.Customer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IReportsService {
    List<Customer> getCustomersWithMaxBookings();
    Map<String, Long> getCarsWithMinimalBookings();
    Map<String, BigDecimal> getIncomeByCarModel();
    BigDecimal getRevenueForPeriod(LocalDate startDate, LocalDate endDate);
    Map<String, BigDecimal> getMaintenanceCostByCarModel();
    Map<String, BigDecimal> getMaintenanceCostByPeriod(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getDashboardStatistics();
    Map<String, Long> getCarUtilizationReport();
    Map<String, Object> getCustomerLoyaltyAnalytics();
    Map<String, BigDecimal> getProfitabilityReport();
    Map<String, Object> getFleetHealthReport();
    List<Map<String, Object>> getServiceCenterPerformance();
    Map<String, Object> getBookingTrends(LocalDate startDate, LocalDate endDate);
}
