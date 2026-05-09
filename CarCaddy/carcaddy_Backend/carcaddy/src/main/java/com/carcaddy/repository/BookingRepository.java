package com.carcaddy.repository;

import com.carcaddy.entity.Booking;
import com.carcaddy.entity.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //  1. Find bookings by Customer
    List<Booking> findByCustomer_CustomerId(String customerId);


    //  2. Find bookings by Car (registration number)
    List<Booking> findByCar_RegistrationNumber(String registrationNumber);


    //  3. Find bookings by status
    List<Booking> findByBookingStatus(BookingStatus status);


    //  4. Find active bookings (CONFIRMED + ACTIVE)
    List<Booking> findByBookingStatusIn(List<BookingStatus> statuses);


    //  5.  MOST IMPORTANT → Overlap check (used in createBooking)
    @Query("""
        SELECT b FROM Booking b
        WHERE b.car.registrationNumber = :regNo
        AND b.bookingStatus != 'CANCELLED'
        AND b.startDate <= :endDate
        AND b.endDate >= :startDate
    """)
    List<Booking> findOverlappingBookings(
            @Param("regNo") String regNo,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    //  6.  Overlap check excluding same booking (used in modifyBooking)
    @Query("""
        SELECT b FROM Booking b
        WHERE b.car.registrationNumber = :regNo
        AND b.bookingStatus != 'CANCELLED'
        AND b.bookingId != :bookingId
        AND b.startDate <= :endDate
        AND b.endDate >= :startDate
    """)
    List<Booking> findOverlappingBookingsExcludingId(
            @Param("regNo") String regNo,
            @Param("bookingId") Long bookingId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}