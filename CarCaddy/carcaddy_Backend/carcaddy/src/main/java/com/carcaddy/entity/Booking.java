package com.carcaddy.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

//import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
//import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "mileage_at_return")
    private Double mileageAtReturn;

    @Column(name = "mileage_at_start")
    private Double mileageAtStart;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "total_fare")
    private Double totalFare;

      
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "customer_id", nullable = false)
    // private Customer customer;

    //  CAR MAPPING (registration_number is PK in Car table)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "registration_number", referencedColumnName = "registration_number", nullable = false)
    // @JsonBackReference
    // private Car car;

    


    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
