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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsServiceImpl implements IReportsService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReportsServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private EmployeeRespository employeeRepository;

    // =========================================================
    // CUSTOMERS WITH MAX BOOKINGS
    // =========================================================

    @Override
    public List<Customer> getCustomersWithMaxBookings() {

        logger.info("Generating customers with maximum bookings");

        List<Booking> bookings = bookingRepository.findAll();

        Map<Customer, Long> bookingCountMap =
                bookings.stream()
                        .filter(b -> b.getCustomer() != null)
                        .collect(Collectors.groupingBy(
                                Booking::getCustomer,
                                Collectors.counting()
                        ));

        long maxBookings =
                bookingCountMap.values()
                        .stream()
                        .max(Long::compare)
                        .orElse(0L);

        return bookingCountMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == maxBookings)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // =========================================================
    // CARS WITH MINIMAL BOOKINGS
    // =========================================================

    @Override
    public Map<String, Long> getCarsWithMinimalBookings() {

        logger.info("Generating cars with minimal bookings");

        List<Car> cars = carRepository.findAll();

        Map<String, Long> result = new LinkedHashMap<>();

        for (Car car : cars) {

            long count =
                    bookingRepository
                            .findByCar_RegistrationNumber(
                                    car.getRegistrationNumber()
                            )
                            .size();

            result.put(
                    car.getRegistrationNumber()
                            + " (" + car.getModel() + ")",
                    count
            );
        }

        return result.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // =========================================================
    // INCOME BY CAR MODEL
    // =========================================================

    @Override
    public Map<String, BigDecimal> getIncomeByCarModel() {

        logger.info("Generating income by car model");

        List<Booking> bookings = bookingRepository.findAll();

        Map<String, BigDecimal> result = new HashMap<>();

        for (Booking booking : bookings) {

            if (booking.getCar() != null &&
                    booking.getTotalFare() != null) {

                String model =
                        booking.getCar().getModel();

                BigDecimal fare =
                        BigDecimal.valueOf(
                                booking.getTotalFare()
                        );

                result.merge(
                        model,
                        fare,
                        BigDecimal::add
                );
            }
        }

        return result;
    }

    // =========================================================
    // REVENUE FOR PERIOD
    // =========================================================

    @Override
    public BigDecimal getRevenueForPeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {

        logger.info("Generating revenue report");

        List<Booking> bookings =
                bookingRepository.findAll();

        return bookings.stream()
                .filter(b ->
                        b.getCreatedAt() != null &&
                        !b.getCreatedAt().toLocalDate()
                                .isBefore(startDate) &&
                        !b.getCreatedAt().toLocalDate()
                                .isAfter(endDate)
                )
                .map(b -> BigDecimal.valueOf(
                        Optional.ofNullable(
                                b.getTotalFare()
                        ).orElse(0.0)
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =========================================================
    // MAINTENANCE COST BY CAR MODEL
    // =========================================================

    @Override
    public Map<String, BigDecimal>
    getMaintenanceCostByCarModel() {

        logger.info("Generating maintenance cost by car model");

        List<Maintenance> maintenanceList =
                maintenanceRepository.findAll();

        Map<String, BigDecimal> result =
                new HashMap<>();

        for (Maintenance maintenance : maintenanceList) {

            Optional<Car> optionalCar =
                    carRepository.findById(
                            maintenance.getRegistrationNumber()
                    );

            if (optionalCar.isPresent()) {

                String model =
                        optionalCar.get().getModel();

                BigDecimal cost =
                        BigDecimal.valueOf(
                                maintenance.getCost()
                        );

                result.merge(
                        model,
                        cost,
                        BigDecimal::add
                );
            }
        }

        return result;
    }

    // =========================================================
    // MAINTENANCE COST BY PERIOD
    // =========================================================

    @Override
    public Map<String, BigDecimal>
    getMaintenanceCostByPeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {

        logger.info("Generating maintenance cost by period");

        List<Maintenance> maintenanceList =
                maintenanceRepository.findAll();

        Map<String, BigDecimal> result =
                new HashMap<>();

        for (Maintenance maintenance : maintenanceList) {

            if (maintenance.getScheduledDate() != null &&
                    !maintenance.getScheduledDate()
                            .isBefore(startDate) &&
                    !maintenance.getScheduledDate()
                            .isAfter(endDate)) {

                String key =
                        maintenance.getScheduledDate()
                                .getYear()
                                + "-"
                                + maintenance.getScheduledDate()
                                .getMonthValue();

                result.merge(
                        key,
                        BigDecimal.valueOf(
                                maintenance.getCost()
                        ),
                        BigDecimal::add
                );
            }
        }

        return result;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @Override
    public Map<String, Object>
    getDashboardStatistics() {

        logger.info("Generating dashboard statistics");

        Map<String, Object> stats =
                new HashMap<>();

        stats.put(
                "totalEmployees",
                employeeRepository.count()
        );

        stats.put(
                "totalCars",
                carRepository.count()
        );

        stats.put(
                "totalCustomers",
                customerRepository.count()
        );

        stats.put(
                "totalRentals",
                bookingRepository.count()
        );

        stats.put(
                "totalMaintenanceRecords",
                maintenanceRepository.count()
        );

        return stats;
    }

    // =========================================================
    // CAR UTILIZATION
    // =========================================================

    @Override
    public Map<String, Long>
    getCarUtilizationReport() {

        logger.info("Generating car utilization report");

        Map<String, Long> result =
                new LinkedHashMap<>();

        List<Car> cars = carRepository.findAll();

        for (Car car : cars) {

            long completedBookings =
                    bookingRepository
                            .findByCar_RegistrationNumber(
                                    car.getRegistrationNumber()
                            )
                            .stream()
                            .filter(b ->
                                    b.getBookingStatus()
                                            == BookingStatus.COMPLETED
                            )
                            .count();

            result.put(
                    car.getRegistrationNumber(),
                    completedBookings
            );
        }

        return result;
    }

    // =========================================================
    // CUSTOMER LOYALTY
    // =========================================================

    @Override
    public Map<String, Object>
    getCustomerLoyaltyAnalytics() {

        logger.info("Generating customer loyalty analytics");

        List<Customer> customers =
                customerRepository.findAll();

        Map<String, Object> result =
                new HashMap<>();

        int totalPoints =
                customers.stream()
                        .mapToInt(c ->
                                Optional.ofNullable(
                                        c.getLoyaltyPoints()
                                ).orElse(0)
                        )
                        .sum();

        double avgPoints =
                customers.stream()
                        .mapToInt(c ->
                                Optional.ofNullable(
                                        c.getLoyaltyPoints()
                                ).orElse(0)
                        )
                        .average()
                        .orElse(0);

        result.put("totalCustomers", customers.size());

        result.put("totalLoyaltyPoints", totalPoints);

        result.put("averageLoyaltyPoints", avgPoints);

        return result;
    }

    // =========================================================
    // PROFITABILITY REPORT
    // =========================================================

    @Override
    public Map<String, BigDecimal>
    getProfitabilityReport() {

        logger.info("Generating profitability report");

        Map<String, BigDecimal> income =
                getIncomeByCarModel();

        Map<String, BigDecimal> maintenance =
                getMaintenanceCostByCarModel();

        Map<String, BigDecimal> result =
                new HashMap<>();

        for (String model : income.keySet()) {

            BigDecimal revenue =
                    income.getOrDefault(
                            model,
                            BigDecimal.ZERO
                    );

            BigDecimal maintenanceCost =
                    maintenance.getOrDefault(
                            model,
                            BigDecimal.ZERO
                    );

            result.put(
                    model,
                    revenue.subtract(maintenanceCost)
            );
        }

        return result;
    }

    // =========================================================
    // FLEET HEALTH
    // =========================================================

    @Override
    public Map<String, Object>
    getFleetHealthReport() {

        logger.info("Generating fleet health report");

        List<Car> cars = carRepository.findAll();

        long availableCars =
                cars.stream()
                        .filter(c ->
                                c.getStatus()
                                        == CarStatus.AVAILABLE
                        )
                        .count();

        long rentedCars =
                cars.stream()
                        .filter(c ->
                                c.getStatus()
                                        == CarStatus.RENTED
                        )
                        .count();

        long maintenanceCars =
                cars.stream()
                        .filter(c ->
                                c.getStatus()
                                        == CarStatus.MAINTENANCE
                        )
                        .count();

        Map<String, Object> result =
                new HashMap<>();

        result.put("totalCars", cars.size());

        result.put("availableCars", availableCars);

        result.put("rentedCars", rentedCars);

        result.put("maintenanceCars", maintenanceCars);

        result.put(
                "fleetUtilization",
                cars.size() == 0
                        ? 0
                        : ((double) rentedCars / cars.size()) * 100
        );

        return result;
    }
}