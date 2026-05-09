package com.carcaddy.controller;

import com.carcaddy.dto.CreateBookingRequest;
import com.carcaddy.dto.ModifyBookingRequest;
import com.carcaddy.dto.ReturnCarRequest;
import com.carcaddy.entity.Booking;
import com.carcaddy.service.IBookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private IBookingService bookingService;

    //  1. Create Booking
    @PostMapping
    public Booking createBooking(@RequestBody CreateBookingRequest request) {
        System.out.println("touched");
        return bookingService.createBooking(request);
    }

    //  2. Modify Booking
    @PutMapping("/{bookingId}")
    public Booking modifyBooking(
            @PathVariable Long bookingId,
            @RequestBody ModifyBookingRequest request) {
        return bookingService.modifyBooking(bookingId, request);
    }

    //  3. Cancel Booking
    @DeleteMapping("/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId) {
        return bookingService.cancelBooking(bookingId);
    }

    //  4. Return Car (Acknowledge Return)
    @PostMapping("/{bookingId}/return")
    public Booking returnCar(
            @PathVariable Long bookingId,
            @RequestBody ReturnCarRequest request) {
        return bookingService.returnCar(bookingId, request);
    }

    //  5. Check Car Availability
    @GetMapping("/availability")
    public boolean checkAvailability(
            @RequestParam String registrationNumber,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return bookingService.checkCarAvailability(
                registrationNumber, startDate, endDate
        );
    }

    //  6. Get All Bookings
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    //  7. Get Booking by ID
    @GetMapping("/{bookingId}")
    public Booking getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    //  8. Get Bookings by Customer
    @GetMapping("/customer/{customerId}")
    public List<Booking> getBookingsByCustomer(
            @PathVariable String customerId) {
        return bookingService.getBookingsByCustomer(customerId);
    }

    //  9. Get Bookings by Car
    @GetMapping("/car/{registrationNumber}")
    public List<Booking> getBookingsByCar(
            @PathVariable String registrationNumber) {
        return bookingService.getBookingsByCar(registrationNumber);
    }

    //  10. Get Active Bookings
    @GetMapping("/active")
    public List<Booking> getActiveBookings() {
        return bookingService.getActiveBookings();
    }

    //  11. Get Completed Bookings
    @GetMapping("/completed")
    public List<Booking> getCompletedBookings() {
        return bookingService.getCompletedBookings();
    }

}

