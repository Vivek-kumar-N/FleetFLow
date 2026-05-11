
     
package com.carcaddy.entity;
 
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "car")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Car {
 
 
 
    @Id
    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;
 
    /* ---------------- Car Details ---------------- */
 
    @Column(name = "category")
    private String category;
 
    @Column(name = "color")
    private String color;
 
    @Column(name = "car_condition")
    private String carCondition;
 
    @Column(name = "insurance_number", nullable = false, unique = true)
    private String insuranceNumber;
 
    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;
 
    @Column(name = "last_service_mileage")
    private Double lastServiceMileage;
 
    @Column(name = "mileage")
    private Double mileage;
 
    @Column(name = "model", nullable = false)
    private String model;
 
    @Column(name = "registration_date")
    private LocalDate registrationDate;
 
    /* ---------------- Rental Info ---------------- */
 
    @Column(name = "rental_count")
    private Integer rentalCount;
 
    @Column(name = "rental_rate_per_day")
    private Double rentalRatePerDay;
 
    /* ---------------- Status ---------------- */
 
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CarStatus status;
 
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    @JsonIgnore
    @JsonManagedReference
    private List<Booking> bookings;
}




