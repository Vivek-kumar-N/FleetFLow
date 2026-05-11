package com.carcaddy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Registration number is required")
    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    /* ---------------- Car Details ---------------- */

    @NotBlank(message = "Category is required")
    @Column(name = "category")
    private String category;

    @NotBlank(message = "Color is required")
    @Column(name = "color")
    private String color;

    @NotBlank(message = "Car condition is required")
    @Column(name = "car_condition")
    private String carCondition;

    @NotBlank(message = "Insurance number is required")
    @Column(name = "insurance_number", nullable = false, unique = true)
    private String insuranceNumber;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @PositiveOrZero(message = "Last service mileage cannot be negative")
    @Column(name = "last_service_mileage")
    private Double lastServiceMileage;

    @PositiveOrZero(message = "Mileage cannot be negative")
    @Column(name = "mileage")
    private Double mileage;

    @NotBlank(message = "Model is required")
    @Size(min = 2, max = 50, message = "Model must be between 2 and 50 characters")
    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    /* ---------------- Rental Info ---------------- */

    @PositiveOrZero(message = "Rental count cannot be negative")
    @Column(name = "rental_count")
    private Integer rentalCount;

    @Positive(message = "Rental rate per day must be positive")
    @Column(name = "rental_rate_per_day")
    private Double rentalRatePerDay;

    /* ---------------- Status ---------------- */

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CarStatus status;

    /* ---------------- Booking Relationship ---------------- */

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    @JsonIgnore
    @JsonManagedReference
    private List<Booking> bookings;

    /* ---------------- Maintenance Relationship ---------------- */

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Maintenance> maintenances;
}