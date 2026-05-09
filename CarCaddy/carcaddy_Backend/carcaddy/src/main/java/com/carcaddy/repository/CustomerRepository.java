package com.carcaddy.repository;

import com.carcaddy.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {


    // View customers by name (case-insensitive)
    List<Customer> findByCustomerNameContainingIgnoreCase(String customerName);

    // View customer by email (email_id is UNIQUE)
    Optional<Customer> findByEmailId(String emailId);

    // View customer by driving license (driving_license is UNIQUE)
    Optional<Customer> findByDrivingLicense(String drivingLicense);



    // Used by Booking module to prevent new bookings
    boolean existsByCustomerIdAndBlacklistedTrue(String customerId);



    // Update customer contact number
    @Modifying
    @Query("""
        UPDATE Customer c
        SET c.contactNumber = :contactNumber
        WHERE c.customerId = :customerId
    """)
    void updateCustomerContactNumber(String customerId, String contactNumber);

    // Add loyalty points after successful booking completion
    @Modifying
    @Query("""
        UPDATE Customer c
        SET c.loyaltyPoints = c.loyaltyPoints + :points
        WHERE c.customerId = :customerId
    """)
    void addLoyaltyPoints(String customerId, int points);

    // Redeem loyalty points
    @Modifying
    @Query("""
        UPDATE Customer c
        SET c.loyaltyPoints = c.loyaltyPoints - :points
        WHERE c.customerId = :customerId
    """)
    void redeemLoyaltyPoints(String customerId, int points);

    

    // No entity mapping required
    @Query(value = """
        SELECT customer_id
        FROM booking
        GROUP BY customer_id
        ORDER BY COUNT(booking_id) DESC
    """, nativeQuery = true)
    List<String> findCustomerIdsWithMaximumBookings();



    // Fetch booking IDs for a customer (used for history tracking)
    @Query(value = """
        SELECT booking_id
        FROM booking
        WHERE customer_id = :customerId
    """, nativeQuery = true)
    List<Long> findBookingIdsByCustomerId(String customerId);
 
}
