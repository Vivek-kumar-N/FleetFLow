package com.carcaddy.controller;

import com.carcaddy.dto.CreateBookingRequest;
import com.carcaddy.dto.ModifyBookingRequest;
import com.carcaddy.dto.ReturnCarRequest;
import com.carcaddy.entity.Booking;
import com.carcaddy.service.IBookingService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private IBookingService bookingService;

    //  1. CREATE BOOKING
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("Create booking request received for customer {}", request.getCustomerId());

        Booking booking = bookingService.createBooking(request);

        return ResponseEntity.ok().body(booking);
    }

    //  2. MODIFY BOOKING
    @PutMapping("/{bookingId}")
    public ResponseEntity<?> modifyBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody ModifyBookingRequest request) {

        log.info("Modify booking request for bookingId {}", bookingId);

        Booking booking = bookingService.modifyBooking(bookingId, request);

        return ResponseEntity.ok().body(booking);
    }

    //  3. CANCEL BOOKING
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {

        log.info("Cancel booking request for bookingId {}", bookingId);

        String response = bookingService.cancelBooking(bookingId);

        return ResponseEntity.ok().body(response);
    }

    //  4. RETURN CAR
    @PostMapping("/{bookingId}/return")
    public ResponseEntity<?> returnCar(
            @PathVariable Long bookingId,
            @RequestBody ReturnCarRequest request) {

        log.info("Return car request for bookingId {}", bookingId);

        Booking booking = bookingService.returnCar(bookingId, request);

        return ResponseEntity.ok().body(booking);
    }

    //  5. CHECK AVAILABILITY
    @GetMapping("/availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam String registrationNumber,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        log.info("Check availability for car {} from {} to {}", registrationNumber, startDate, endDate);

        boolean available = bookingService.checkCarAvailability(
                registrationNumber, startDate, endDate
        );

        return ResponseEntity.ok().body(available);
    }

    //  6. GET ALL BOOKINGS
    @GetMapping
    public ResponseEntity<?> getAllBookings() {

        log.info("Fetch all bookings request");

        List<Booking> bookings = bookingService.getAllBookings();

        return ResponseEntity.ok().body(bookings);
    }

    //  7. GET BOOKING BY ID
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {

        log.info("Fetch booking by ID {}", bookingId);

        Booking booking = bookingService.getBookingById(bookingId);

        return ResponseEntity.ok().body(booking);
    }

    //  8. GET BOOKINGS BY CUSTOMER
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getBookingsByCustomer(
            @PathVariable String customerId) {

        log.info("Fetch bookings for customer {}", customerId);

        List<Booking> bookings = bookingService.getBookingsByCustomer(customerId);

        return ResponseEntity.ok().body(bookings);
    }

    //  9. GET BOOKINGS BY CAR
    @GetMapping("/car/{registrationNumber}")
    public ResponseEntity<?> getBookingsByCar(
            @PathVariable String registrationNumber) {

        log.info("Fetch bookings for car {}", registrationNumber);

        List<Booking> bookings = bookingService.getBookingsByCar(registrationNumber);

        return ResponseEntity.ok().body(bookings);
    }

    //  10. GET ACTIVE BOOKINGS
    @GetMapping("/active")
    public ResponseEntity<?> getActiveBookings() {

        log.info("Fetch active bookings");

        List<Booking> bookings = bookingService.getActiveBookings();

        return ResponseEntity.ok().body(bookings);
    }

    //  11. GET COMPLETED BOOKINGS
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedBookings() {

        log.info("Fetch completed bookings");

        List<Booking> bookings = bookingService.getCompletedBookings();

        return ResponseEntity.ok().body(bookings);
    }
}
