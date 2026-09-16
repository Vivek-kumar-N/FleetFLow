package com.carcaddy.service;

import java.time.LocalDate;
import java.util.List;

import com.carcaddy.dto.CreateBookingRequest;
import com.carcaddy.dto.ModifyBookingRequest;
import com.carcaddy.dto.ReturnCarRequest;
import com.carcaddy.entity.Booking;

public interface IBookingService {

    //  1. Create Booking (full logic: customer + car + discount)
    Booking createBooking(CreateBookingRequest request);


    //  2. Modify Booking (dates + car reallocation + fare recalculation)
    Booking modifyBooking(Long bookingId, ModifyBookingRequest request);


    //  3. Cancel Booking (release car + update status)
    String cancelBooking(Long bookingId);


    //  4. Return Car (complete lifecycle)
    Booking returnCar(Long bookingId, ReturnCarRequest request);


    //  5. Check Car Availability (overlap logic)
    boolean checkCarAvailability(String registrationNumber,
                                 LocalDate startDate,
                                 LocalDate endDate);


    //  6. View All Bookings
    List<Booking> getAllBookings();


    //  7. View Booking by ID
    Booking getBookingById(Long bookingId);


    //  8. View Bookings by Customer
    List<Booking> getBookingsByCustomer(String customerId);


    //  9. View Bookings by Car
    List<Booking> getBookingsByCar(String registrationNumber);


    //  10. View Active / Ongoing Bookings
    List<Booking> getActiveBookings();


    //  11. View Completed Bookings
    List<Booking> getCompletedBookings();

}