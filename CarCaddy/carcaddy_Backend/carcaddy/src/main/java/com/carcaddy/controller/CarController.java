package com.carcaddy.controller;

import com.carcaddy.entity.Car;
import com.carcaddy.entity.CarStatus;
import com.carcaddy.service.ICarService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CarController {

    private final ICarService carService;

    /* ---------------- ADD ---------------- */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Car addCar(@Valid @RequestBody Car car) {

        log.info("API request: Add car with registration number {}",
                car.getRegistrationNumber());

        return carService.addCar(car);
    }

    /* ---------------- UPDATE ---------------- */

    @PutMapping("/{registrationNumber}")
    public Car updateCar(
            @PathVariable String registrationNumber,
            @Valid @RequestBody Car car) {

        log.info("API request: Update car details for {}",
                registrationNumber);

        return carService.updateCar(registrationNumber, car);
    }

    @PatchMapping("/{registrationNumber}/status")
    public Car updateCarStatus(
            @PathVariable String registrationNumber,
            @RequestBody(required = false) java.util.Map<String, String> body,
            @RequestParam(required = false) CarStatus status) {

        CarStatus finalStatus = status;
        if (finalStatus == null && body != null && body.get("status") != null) {
            finalStatus = CarStatus.valueOf(body.get("status"));
        }

        log.info("API request: Update car status for {} to {}",
                registrationNumber, finalStatus);

        return carService.updateCarStatus(registrationNumber, finalStatus);
    }

    @PatchMapping("/{registrationNumber}/mileage")
    public Car updateMileage(
            @PathVariable String registrationNumber,
            @RequestParam Double mileage) {

        log.info("API request: Update mileage for {} to {}",
                registrationNumber, mileage);

        return carService.updateMileageAfterRental(registrationNumber, mileage);
    }

    /* ---------------- FETCH ---------------- */

    @GetMapping
    public List<Car> getAllCars() {

        log.info("API request: Fetch all cars");

        return carService.getAllCars();
    }

    @GetMapping("/{registrationNumber}")
    public Car getCarByRegistrationNumber(
            @PathVariable String registrationNumber) {

        log.info("API request: Fetch car with registration number {}",
                registrationNumber);

        return carService.getCarByRegistrationNumber(registrationNumber);
    }

    @GetMapping("/model/{model}")
    public List<Car> getCarsByModel(@PathVariable String model) {

        log.info("API request: Fetch cars by model {}",
                model);

        return carService.getCarsByModel(model);
    }

    @GetMapping("/category/{category}")
    public List<Car> getCarsByCategory(@PathVariable String category) {

        log.info("API request: Fetch cars by category {}",
                category);

        return carService.getCarsByCategory(category);
    }

    @GetMapping("/status/{status}")
    public List<Car> getCarsByStatus(@PathVariable CarStatus status) {

        log.info("API request: Fetch cars by status {}",
                status);

        return carService.getCarsByStatus(status);
    }

    @GetMapping("/available")
    public List<Car> getAvailableCars() {

        log.info("API request: Fetch available cars");

        return carService.getAvailableCars();
    }

    @GetMapping("/maintenance")
    public List<Car> getCarsRequiringMaintenance() {

        log.info("API request: Fetch cars requiring maintenance");

        return carService.getCarsRequiringMaintenance();
    }
}