package com.carcaddy.repository;
 

import com.carcaddy.entity.Car;
import com.carcaddy.entity.CarStatus;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
 
public interface CarRepository extends JpaRepository<Car, String> {
 
    //  1. Find by Primary Key (registration number)
    Optional<Car> findByRegistrationNumber(String registrationNumber);
 
 
    //  2. Get cars by category + status (OLD FLOW - still useful)
    List<Car> findByCategoryAndStatus(String category, CarStatus status);
 
 
    //  3.  Get cars by model + status (USED IN YOUR FINAL CREATE BOOKING)
    List<Car> findByModelAndStatus(String model, CarStatus status);
 
 
    //  4. Get all cars by category
    List<Car> findByCategory(String category);
 
 
    //  5. Get all cars by model
    List<Car> findByModel(String model);
 
 
    //  6. Get all cars by status (AVAILABLE / RENTED / MAINTENANCE)
    List<Car> findByStatus(CarStatus status);
 
 
    //  7.  Get DISTINCT models for a category (FOR DROPDOWN UI)
    @Query("""
        SELECT DISTINCT c.model FROM Car c
        WHERE c.category = :category
    """)
    List<String> findDistinctModelsByCategory(@Param("category") String category);
 
 
    //  8.  OPTIONAL: Cars needing service (useful in returnCar)
    @Query("""
        SELECT c FROM Car c
        WHERE c.mileage - c.lastServiceMileage >= 5000
    """)
    List<Car> findCarsDueForService();
 
 
    //  9.  OPTIONAL: Cars by condition (good/damaged)
    List<Car> findByCarCondition(String carCondition);
 
 
    //  10.  OPTIONAL: Cars by rental count (frequent usage)
    List<Car> findByRentalCountGreaterThan(Integer rentalCount);
 
}