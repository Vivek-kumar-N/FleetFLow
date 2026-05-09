package com.carcaddy.controller;

import com.carcaddy.entity.Car;
import com.carcaddy.entity.CarStatus;
import com.carcaddy.service.impl.CarServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarServiceImpl carService;

    /* ---------------- ADD ---------------- */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Car addCar(@RequestBody Car car) {
        return carService.addCar(car);
    }

    /* ---------------- UPDATE ---------------- */
    @PutMapping("/{registrationNumber}")
    public Car updateCar(
            @PathVariable String registrationNumber,
            @RequestBody Car car) {
        return carService.updateCar(registrationNumber, car);
    }

    @PatchMapping("/{registrationNumber}/status")
    public Car updateCarStatus(
            @PathVariable String registrationNumber,
            @RequestParam CarStatus status) {
        return carService.updateCarStatus(registrationNumber, status);
    }

    @PatchMapping("/{registrationNumber}/mileage")
    public Car updateMileage(
            @PathVariable String registrationNumber,
            @RequestParam Double mileage) {
        return carService.updateMileageAfterRental(registrationNumber, mileage);
    }

    /* ---------------- GET ---------------- */
    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/{registrationNumber}")
    public Car getCarByRegistrationNumber(@PathVariable String registrationNumber) {
        return carService.getCarByRegistrationNumber(registrationNumber);
    }

    @GetMapping("/model/{model}")
    public List<Car> getCarsByModel(@PathVariable String model) {
        return carService.getCarsByModel(model);
    }

    @GetMapping("/category/{category}")
    public List<Car> getCarsByCategory(@PathVariable String category) {
        return carService.getCarsByCategory(category);
    }

    @GetMapping("/status/{status}")
    public List<Car> getCarsByStatus(@PathVariable CarStatus status) {
        return carService.getCarsByStatus(status);
    }

    @GetMapping("/available")
    public List<Car> getAvailableCars() {
        return carService.getAvailableCars();
    }

    @GetMapping("/maintenance")
    public List<Car> getCarsRequiringMaintenance() {
        return carService.getCarsRequiringMaintenance();
    }
}
