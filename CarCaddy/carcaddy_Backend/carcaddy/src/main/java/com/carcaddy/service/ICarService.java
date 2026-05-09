package com.carcaddy.service;


import com.carcaddy.entity.Car;
import com.carcaddy.entity.CarStatus;

import java.util.List;

public interface ICarService {

    /* -------- CRUD -------- */
    Car addCar(Car car);

    Car updateCar(String registrationNumber, Car car);

    Car updateCarStatus(String registrationNumber, CarStatus status);

    Car updateMileageAfterRental(String registrationNumber, Double newMileage);

    /* -------- Fetch -------- */
    List<Car> getAllCars();

    Car getCarByRegistrationNumber(String registrationNumber);

    List<Car> getCarsByModel(String model);

    List<Car> getCarsByCategory(String category);

    List<Car> getCarsByStatus(CarStatus status);

    List<Car> getAvailableCars();

    List<Car> getCarsRequiringMaintenance();

    /* -------- Business -------- */
    boolean needsMaintenance(Car car);
}