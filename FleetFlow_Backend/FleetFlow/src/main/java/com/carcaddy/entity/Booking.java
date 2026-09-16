package com.carcaddy.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

//import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
//import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;




@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @NotNull(message = "Booking status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private Double discount;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @DecimalMin(value = "0.0", message = "Mileage at return cannot be negative")
    @Column(name = "mileage_at_return")
    private Double mileageAtReturn;

    @NotNull(message = "Mileage at start is required")
    @DecimalMin(value = "0.0", message = "Mileage must be positive")
    @Column(name = "mileage_at_start")
    private Double mileageAtStart;

    @NotNull(message = "Passenger count is required")
    @Min(value = 1, message = "Passenger must be at least 1")
    @Max(value = 10, message = "Passenger cannot exceed 10")
    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Total fare is required")
    @DecimalMin(value = "0.0", message = "Total fare cannot be negative")
    @Column(name = "total_fare")
    private Double totalFare;


      
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_number", referencedColumnName = "registration_number", nullable = false)
    private Car car;

    


    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
