package com.carcaddy.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carcaddy.dto.CreateBookingRequest;
import com.carcaddy.dto.ModifyBookingRequest;
import com.carcaddy.dto.ReturnCarRequest;
import com.carcaddy.entity.*;
import com.carcaddy.repository.*;
import com.carcaddy.service.IBookingService;

@Service
public class BookingServiceImpl implements IBookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    // ✅ 1. CREATE BOOKING
    @Override
    public Booking createBooking(CreateBookingRequest request) {

        log.info("CreateBooking API START");

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (Boolean.TRUE.equals(customer.getBlacklisted())) {
            throw new RuntimeException("Customer is blacklisted: " + customer.getBlacklistReason());
        }

        log.info("Customer found: {}", customer.getCustomerId());

        List<Car> cars = carRepository.findByModelAndStatus(
                request.getModel(), CarStatus.AVAILABLE
        );

        log.info("Cars fetched: {}", cars.size());

        for (Car c : cars) {
            log.info("Car: {}, Model: {}, Status: {}",
                    c.getRegistrationNumber(),
                    c.getModel(),
                    c.getStatus());
        }

        List<Car> availableCars = new ArrayList<>();

        for (Car car : cars) {

            boolean available = checkCarAvailability(
                    car.getRegistrationNumber(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            log.info("Checking availability for {} → {}",
                    car.getRegistrationNumber(), available);

            if (available) {
                availableCars.add(car);
            }
        }

        log.info("Final available cars: {}", availableCars.size());

        if (availableCars.isEmpty()) {
            throw new RuntimeException("No available cars");
        }

        Car selectedCar = availableCars.get(new Random().nextInt(availableCars.size()));

        log.info("Selected car: {}", selectedCar.getRegistrationNumber());

        long days = ChronoUnit.DAYS.between(
                request.getStartDate(), request.getEndDate()
        );

        if (days == 0) days = 1;

        double baseFare = days * selectedCar.getRentalRatePerDay();

        double discount = 0;
        if (customer.getLoyaltyPoints() >= 500) {
            discount = selectedCar.getRentalRatePerDay();
        } else if (customer.getLoyaltyPoints() >= 100) {
            discount = baseFare * 0.05;
        }

        log.info("Base fare: {}", baseFare);
        log.info("Discount: {}", discount);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setCar(selectedCar);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setPassengerCount(request.getPassengerCount());
        booking.setMileageAtStart(selectedCar.getMileage());
        booking.setDiscount(discount);
        booking.setTotalFare(baseFare - discount);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);

        selectedCar.setStatus(CarStatus.RENTED);
        selectedCar.setRentalCount(selectedCar.getRentalCount() + 1);
        carRepository.save(selectedCar);

        log.info("Booking CREATED successfully: {}", saved.getBookingId());

        return saved;
    }

    // ✅ 2. MODIFY BOOKING
    @Override
    public Booking modifyBooking(Long bookingId, ModifyBookingRequest request) {

        log.info("ModifyBooking START for bookingId {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED ||
            booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot modify this booking");
        }

        LocalDate newStart = request.getStartDate() != null
                ? request.getStartDate()
                : booking.getStartDate();

        LocalDate newEnd = request.getEndDate() != null
                ? request.getEndDate()
                : booking.getEndDate();

        log.info("New dates → Start: {}, End: {}", newStart, newEnd);

        List<Booking> conflicts = bookingRepository.findOverlappingBookingsExcludingId(
                booking.getCar().getRegistrationNumber(),
                bookingId,
                newStart,
                newEnd
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Dates not available");
        }

        booking.setStartDate(newStart);
        booking.setEndDate(newEnd);

        if (request.getModel() != null) {

            log.info("Car change requested → Model: {}", request.getModel());

            Car oldCar = booking.getCar();

            List<Car> cars = carRepository.findByModelAndStatus(
                    request.getModel(), CarStatus.AVAILABLE
            );

            if (cars.isEmpty()) {
                throw new RuntimeException("No cars available for model");
            }

            List<Car> availableCars = new ArrayList<>();

            for (Car car : cars) {
                if (checkCarAvailability(car.getRegistrationNumber(), newStart, newEnd)) {
                    availableCars.add(car);
                }
            }

            if (availableCars.isEmpty()) {
                throw new RuntimeException("No cars available after filtering");
            }

            Car newCar = availableCars.get(new Random().nextInt(availableCars.size()));

            log.info("Old car released: {}", oldCar.getRegistrationNumber());

            oldCar.setStatus(CarStatus.AVAILABLE);
            carRepository.save(oldCar);

            booking.setCar(newCar);

            log.info("New car assigned: {}", newCar.getRegistrationNumber());

            newCar.setStatus(CarStatus.RENTED);
            newCar.setRentalCount(newCar.getRentalCount() + 1);
            carRepository.save(newCar);
        }

        long days = ChronoUnit.DAYS.between(newStart, newEnd);
        if (days == 0) days = 1;

        double baseFare = days * booking.getCar().getRentalRatePerDay();

        Customer customer = booking.getCustomer();

        double discount = 0;
        if (customer.getLoyaltyPoints() >= 500) {
            discount = booking.getCar().getRentalRatePerDay();
        } else if (customer.getLoyaltyPoints() >= 100) {
            discount = baseFare * 0.05;
        }

        booking.setTotalFare(baseFare - discount);
        booking.setDiscount(discount);
        booking.setBookingStatus(BookingStatus.MODIFIED);

        log.info("Booking modified successfully: {}", bookingId);

        return bookingRepository.save(booking);
    }

    // ✅ 3. CANCEL BOOKING
    @Override
    public String cancelBooking(Long bookingId) {

        log.info("CancelBooking for bookingId {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        booking.setBookingStatus(BookingStatus.CANCELLED);

        Car car = booking.getCar();
        car.setStatus(CarStatus.AVAILABLE);

        carRepository.save(car);
        bookingRepository.save(booking);

        log.info("Booking cancelled successfully: {}", bookingId);

        return "Booking cancelled successfully";
    }

    // ✅ 4. RETURN CAR
    @Override
    public Booking returnCar(Long bookingId, ReturnCarRequest request) {

        log.info("ReturnCar API for bookingId {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setReturnDate(LocalDate.now());
        booking.setMileageAtReturn(request.getMileageAtReturn());

        Car car = booking.getCar();

        car.setMileage(request.getMileageAtReturn());

        boolean needsMaintenance = false;
        String maintenanceReason = "";

        if (request.isDamaged()) {
            needsMaintenance = true;
            maintenanceReason = request.getDamageNotes() != null && !request.getDamageNotes().isBlank()
                    ? request.getDamageNotes()
                    : "Car returned with damage reported";
        }

        // Check mileage-based maintenance rule (5,000 km since last service)
        if (!needsMaintenance && car.getLastServiceMileage() != null
                && (car.getMileage() - car.getLastServiceMileage()) >= 5_000) {
            needsMaintenance = true;
            maintenanceReason = "Mileage threshold exceeded — service required";
        }

        if (needsMaintenance) {
            log.info("Car {} moved to MAINTENANCE. Reason: {}", car.getRegistrationNumber(), maintenanceReason);
            car.setStatus(CarStatus.MAINTENANCE);

            // Auto-create a maintenance record so it appears in the maintenance list
            Maintenance maintenanceRecord = new Maintenance();
            maintenanceRecord.setCar(car);
            maintenanceRecord.setMaintenanceType(request.isDamaged()
                    ? MaintenanceType.EMERGENCY : MaintenanceType.REPAIR);
            maintenanceRecord.setStatus(MaintenanceStatus.IN_PROGRESS);
            maintenanceRecord.setScheduledDate(LocalDate.now());
            maintenanceRecord.setDescription(maintenanceReason);
            maintenanceRecord.setCost(0);
            maintenanceRecord.setCreatedAt(LocalDateTime.now());
            maintenanceRepository.save(maintenanceRecord);

            log.info("Maintenance record auto-created for car {}", car.getRegistrationNumber());
        } else {
            car.setStatus(CarStatus.AVAILABLE);
        }

        Customer customer = booking.getCustomer();

        int points = (int)(booking.getTotalFare() / 100) * 10;
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);

        customerRepository.save(customer);
        carRepository.save(car);

        log.info("Return completed for bookingId {}", bookingId);

        return bookingRepository.save(booking);
    }

    // ✅ 5. CHECK AVAILABILITY
    @Override
    public boolean checkCarAvailability(String regNo, LocalDate start, LocalDate end) {
        return bookingRepository.findOverlappingBookings(regNo, start, end).isEmpty();
    }

    // ✅ VIEW METHODS

    @Override
    public List<Booking> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(Long id) {
        log.info("Fetching booking by ID {}", id);
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    @Override
    public List<Booking> getBookingsByCustomer(String customerId) {
        log.info("Fetching bookings for customer {}", customerId);
        return bookingRepository.findByCustomer_CustomerId(customerId);
    }

    @Override
    public List<Booking> getBookingsByCar(String regNo) {
        log.info("Fetching bookings for car {}", regNo);
        return bookingRepository.findByCar_RegistrationNumber(regNo);
    }

    @Override
    public List<Booking> getActiveBookings() {
        log.info("Fetching active bookings");
        return bookingRepository.findByBookingStatusIn(
                List.of(BookingStatus.CONFIRMED, BookingStatus.ACTIVE)
        );
    }

    @Override
    public List<Booking> getCompletedBookings() {
        log.info("Fetching completed bookings");
        return bookingRepository.findByBookingStatus(BookingStatus.COMPLETED);
    }
}