
     
package com.carcaddy.entity;
 
import jakarta.persistence.*;
import java.time.LocalDate;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
    name = "car",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "insurance_number")
    }
)
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
 
    /* ---------------- Constructors ---------------- */
 
    public Car() {}
 
    /* ---------------- Getters & Setters ---------------- */
 
    public String getRegistrationNumber() {
        return registrationNumber;
    }
 
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
 
    public String getCategory() {
        return category;
    }
 
    public void setCategory(String category) {
        this.category = category;
    }
 
    public String getColor() {
        return color;
    }
 
    public void setColor(String color) {
        this.color = color;
    }
 
    public String getCarCondition() {
        return carCondition;
    }
 
    public void setCarCondition(String carCondition) {
        this.carCondition = carCondition;
    }
 
    public String getInsuranceNumber() {
        return insuranceNumber;
    }
 
    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }
 
    public LocalDate getLastServiceDate() {
        return lastServiceDate;
    }
 
    public void setLastServiceDate(LocalDate lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }
 
    public Double getLastServiceMileage() {
        return lastServiceMileage;
    }
 
    public void setLastServiceMileage(Double lastServiceMileage) {
        this.lastServiceMileage = lastServiceMileage;
    }
 
    public Double getMileage() {
        return mileage;
    }
 
    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }
 
    public String getModel() {
        return model;
    }
 
    public void setModel(String model) {
        this.model = model;
    }
 
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
 
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
 
    public Integer getRentalCount() {
        return rentalCount;
    }
 
    public void setRentalCount(Integer rentalCount) {
        this.rentalCount = rentalCount;
    }
 
    public Double getRentalRatePerDay() {
        return rentalRatePerDay;
    }
 
    public void setRentalRatePerDay(Double rentalRatePerDay) {
        this.rentalRatePerDay = rentalRatePerDay;
    }
 
    public CarStatus getStatus() {
        return status;
    }
 
    public void setStatus(CarStatus status) {
        this.status = status;
    }
}




