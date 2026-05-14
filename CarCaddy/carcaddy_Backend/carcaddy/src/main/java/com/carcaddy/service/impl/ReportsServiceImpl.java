package com.carcaddy.service.impl;

import com.carcaddy.entity.*;
import com.carcaddy.repository.*;
import com.carcaddy.service.IReportsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReportsServiceImpl implements IReportsService {

    private static final Logger logger = LoggerFactory.getLogger(ReportsServiceImpl.class);

    @Autowired private CustomerRepository customerRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private MaintenanceRepository maintenanceRepository;
    @Autowired private EmployeeRespository employeeRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Customers with MAX bookings
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<Customer> getCustomersWithMaxBookings() {
        logger.info("Generating customers with maximum bookings");

        List<Booking> bookings = bookingRepository.findAll();
        Map<Customer, Integer> bookingCount = new HashMap<>();

        for (Booking b : bookings) {
            if (b.getCustomer() != null) {
                Customer c = b.getCustomer();
                bookingCount.put(c, bookingCount.getOrDefault(c, 0) + 1);
            }
        }

        int max = bookingCount.values().stream().mapToInt(i -> i).max().orElse(0);

        List<Customer> result = new ArrayList<>();
        for (Map.Entry<Customer, Integer> entry : bookingCount.entrySet()) {
            if (entry.getValue() == max) result.add(entry.getKey());
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Cars with MIN bookings
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Long> getCarsWithMinimalBookings() {
        logger.info("Generating cars with minimal bookings");

        List<Car> cars = carRepository.findAll();
        Map<String, Long> result = new LinkedHashMap<>();

        for (Car car : cars) {
            long count = bookingRepository
                    .findByCar_RegistrationNumber(car.getRegistrationNumber()).size();
            String key = car.getRegistrationNumber() + " (" + car.getModel() + ")";
            result.put(key, count);
        }

        List<Map.Entry<String, Long>> list = new ArrayList<>(result.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<String, Long> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Long> e : list) sorted.put(e.getKey(), e.getValue());
        return sorted;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Income by car model
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, BigDecimal> getIncomeByCarModel() {
        logger.info("Generating income by car model");

        List<Booking> bookings = bookingRepository.findAll();
        Map<String, BigDecimal> result = new HashMap<>();

        for (Booking b : bookings) {
            if (b.getCar() != null && b.getTotalFare() != null) {
                String model = b.getCar().getModel();
                BigDecimal fare = BigDecimal.valueOf(b.getTotalFare());
                result.put(model, result.getOrDefault(model, BigDecimal.ZERO).add(fare));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Revenue between dates
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public BigDecimal getRevenueForPeriod(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating revenue report");

        List<Booking> bookings = bookingRepository.findAll();
        BigDecimal total = BigDecimal.ZERO;

        for (Booking b : bookings) {
            if (b.getCreatedAt() != null) {
                LocalDate date = b.getCreatedAt().toLocalDate();
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    double fare = b.getTotalFare() != null ? b.getTotalFare() : 0.0;
                    total = total.add(BigDecimal.valueOf(fare));
                }
            }
        }
        return total;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Maintenance cost by car model
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, BigDecimal> getMaintenanceCostByCarModel() {
        logger.info("Generating maintenance cost by car model");

        List<Maintenance> list = maintenanceRepository.findAll();
        Map<String, BigDecimal> result = new HashMap<>();

        for (Maintenance m : list) {
            Optional<Car> car = carRepository.findById(m.getCar().getRegistrationNumber());
            if (car.isPresent()) {
                String model = car.get().getModel();
                BigDecimal cost = BigDecimal.valueOf(m.getCost());
                result.put(model, result.getOrDefault(model, BigDecimal.ZERO).add(cost));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Maintenance cost by period
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, BigDecimal> getMaintenanceCostByPeriod(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating maintenance cost by period");

        List<Maintenance> list = maintenanceRepository.findAll();
        Map<String, BigDecimal> result = new HashMap<>();

        for (Maintenance m : list) {
            if (m.getScheduledDate() != null) {
                LocalDate date = m.getScheduledDate();
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    String key = date.getYear() + "-" + date.getMonthValue();
                    BigDecimal cost = BigDecimal.valueOf(m.getCost());
                    result.put(key, result.getOrDefault(key, BigDecimal.ZERO).add(cost));
                }
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Dashboard stats
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getDashboardStatistics() {
        logger.info("Generating dashboard statistics");

        Object result = reportRepository.getDashboardStats();
        Object[] data = (Object[]) result;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", data[0]);
        stats.put("totalCars", data[1]);
        stats.put("totalCustomers", data[2]);
        stats.put("totalRentals", data[3]);
        stats.put("totalMaintenanceRecords", data[4]);
        return stats;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Car utilization
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Long> getCarUtilizationReport() {
        logger.info("Generating car utilization report");

        List<Car> cars = carRepository.findAll();
        Map<String, Long> result = new LinkedHashMap<>();

        for (Car car : cars) {
            List<Booking> bookings = bookingRepository
                    .findByCar_RegistrationNumber(car.getRegistrationNumber());
            long count = bookings.stream()
                    .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED)
                    .count();
            result.put(car.getRegistrationNumber(), count);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. Customer loyalty analytics
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getCustomerLoyaltyAnalytics() {
        logger.info("Generating customer loyalty analytics");

        List<Customer> customers = customerRepository.findAll();
        int totalPoints = 0;
        int eligibleForFreeRental = 0;
        int topPoints = 0;

        for (Customer c : customers) {
            int pts = c.getLoyaltyPoints() != null ? c.getLoyaltyPoints() : 0;
            totalPoints += pts;
            if (pts >= 500) eligibleForFreeRental++;
            if (pts > topPoints) topPoints = pts;
        }

        double avg = customers.isEmpty() ? 0 : (double) totalPoints / customers.size();

        Map<String, Object> result = new HashMap<>();
        result.put("totalCustomers", customers.size());
        result.put("totalLoyaltyPoints", totalPoints);
        result.put("totalPointsIssued", totalPoints);
        result.put("averageLoyaltyPoints", avg);
        result.put("customersEligibleForFreeRental", eligibleForFreeRental);
        result.put("topCustomerPoints", topPoints);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. Profitability
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, BigDecimal> getProfitabilityReport() {
        logger.info("Generating profitability report");

        Map<String, BigDecimal> income = getIncomeByCarModel();
        Map<String, BigDecimal> maintenance = getMaintenanceCostByCarModel();
        Map<String, BigDecimal> result = new HashMap<>();

        for (String model : income.keySet()) {
            BigDecimal revenue = income.getOrDefault(model, BigDecimal.ZERO);
            BigDecimal cost = maintenance.getOrDefault(model, BigDecimal.ZERO);
            result.put(model, revenue.subtract(cost));
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 11. Fleet health  (FIXED: adds carsDueForService + averageMileage)
    // Business rules: 5,000 km since last service OR 90 days OR 20 rentals
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getFleetHealthReport() {
        logger.info("Generating fleet health report");

        List<Car> cars = carRepository.findAll();
        int available = 0, rented = 0, maintenance = 0, dueForService = 0;
        double totalMileage = 0;

        for (Car c : cars) {
            if (c.getStatus() == CarStatus.AVAILABLE)        available++;
            else if (c.getStatus() == CarStatus.RENTED)      rented++;
            else if (c.getStatus() == CarStatus.MAINTENANCE) maintenance++;

            if (c.getMileage() != null) totalMileage += c.getMileage();

            boolean mileageRule = c.getLastServiceMileage() != null && c.getMileage() != null
                    && (c.getMileage() - c.getLastServiceMileage()) >= 5_000;
            boolean timeRule = c.getLastServiceDate() != null
                    && ChronoUnit.DAYS.between(c.getLastServiceDate(), LocalDate.now()) >= 90;
            boolean rentalRule = c.getRentalCount() != null && c.getRentalCount() >= 20;

            if (mileageRule || timeRule || rentalRule) dueForService++;
        }

        double utilization = cars.isEmpty() ? 0 : (double) rented / cars.size() * 100;
        double avgMileage   = cars.isEmpty() ? 0 : totalMileage / cars.size();

        Map<String, Object> result = new HashMap<>();
        result.put("totalCars",        cars.size());
        result.put("availableCars",    available);
        result.put("rentedCars",       rented);
        result.put("maintenanceCars",  maintenance);
        result.put("carsDueForService", dueForService);
        result.put("averageMileage",   Math.round(avgMileage));
        result.put("fleetUtilization", utilization);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 12. Service center / mechanic performance  (NEW)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getServiceCenterPerformance() {
        logger.info("Generating service center performance report");

        List<Maintenance> list = maintenanceRepository.findAll();
        Map<String, Map<String, Object>> performerMap = new LinkedHashMap<>();

        for (Maintenance m : list) {
            String performer = (m.getPerformedBy() != null && !m.getPerformedBy().isBlank())
                    ? m.getPerformedBy() : "Unassigned";

            performerMap.putIfAbsent(performer, new HashMap<>());
            Map<String, Object> stats = performerMap.get(performer);

            int jobs = (int) stats.getOrDefault("jobsCompleted", 0);
            double cost = (double) stats.getOrDefault("totalCost", 0.0);

            if (m.getStatus() != null && m.getStatus().name().equals("COMPLETED")) {
                stats.put("jobsCompleted", jobs + 1);
            } else {
                stats.put("jobsCompleted", jobs);
            }
            stats.put("totalCost", cost + m.getCost());
            stats.put("performer", performer);

            if (m.getScheduledDate() != null && m.getCompletedDate() != null) {
                long days = ChronoUnit.DAYS.between(m.getScheduledDate(), m.getCompletedDate());
                double prevAvg   = (double) stats.getOrDefault("avgCompletionDays", 0.0);
                int    prevCount = (int)    stats.getOrDefault("completionCount", 0);
                stats.put("avgCompletionDays", prevCount == 0 ? days
                        : (prevAvg * prevCount + days) / (prevCount + 1));
                stats.put("completionCount", prevCount + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(performerMap.values());
        result.sort((a, b) -> Integer.compare(
                (int) b.getOrDefault("jobsCompleted", 0),
                (int) a.getOrDefault("jobsCompleted", 0)));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 13. Booking trends by period  (NEW)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getBookingTrends(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating booking trends from {} to {}", startDate, endDate);

        List<Booking> bookings = bookingRepository.findAll();
        Map<String, Integer> countByMonth   = new LinkedHashMap<>();
        Map<String, Double>  revenueByMonth = new LinkedHashMap<>();

        for (Booking b : bookings) {
            if (b.getCreatedAt() == null) continue;
            LocalDate date = b.getCreatedAt().toLocalDate();
            if (date.isBefore(startDate) || date.isAfter(endDate)) continue;

            String key = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            countByMonth.put(key, countByMonth.getOrDefault(key, 0) + 1);
            double fare = b.getTotalFare() != null ? b.getTotalFare() : 0.0;
            revenueByMonth.put(key, revenueByMonth.getOrDefault(key, 0.0) + fare);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bookingsByMonth", countByMonth);
        result.put("revenueByMonth",  revenueByMonth);
        return result;
    }
}
