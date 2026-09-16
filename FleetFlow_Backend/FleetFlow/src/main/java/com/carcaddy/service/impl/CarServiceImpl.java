package com.carcaddy.service.impl;

import com.carcaddy.entity.Car;
import com.carcaddy.entity.CarStatus;
import com.carcaddy.exception.InvalidEntityException;
import com.carcaddy.repository.CarRepository;
import com.carcaddy.service.ICarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarServiceImpl implements ICarService {

    private final CarRepository carRepository;

    /* ---------------- ADD ---------------- */
    @Override
    public Car addCar(Car car) {

        log.info("Request received to add car with registration number: {}",
                car.getRegistrationNumber());

        if (carRepository.existsById(car.getRegistrationNumber())) {
            log.warn("Car already exists with registration number: {}",
                    car.getRegistrationNumber());
            throw new InvalidEntityException(
                    "Car already exists with registration number: " + car.getRegistrationNumber()
            );
        }

        car.setRentalCount(0);
        car.setStatus(CarStatus.AVAILABLE);

        Car savedCar = carRepository.save(car);
        log.info("Car added successfully with registration number: {}",
                savedCar.getRegistrationNumber());

        return savedCar;
    }

    /* ---------------- UPDATE ---------------- */
    @Override
    public Car updateCar(String registrationNumber, Car updatedCar) {

        log.info("Updating car details for registration number: {}", registrationNumber);

        Car existingCar = getCarByRegistrationNumber(registrationNumber);

        existingCar.setCategory(updatedCar.getCategory());
        existingCar.setColor(updatedCar.getColor());
        existingCar.setCarCondition(updatedCar.getCarCondition());
        existingCar.setInsuranceNumber(updatedCar.getInsuranceNumber());
        existingCar.setLastServiceDate(updatedCar.getLastServiceDate());
        existingCar.setLastServiceMileage(updatedCar.getLastServiceMileage());
        existingCar.setMileage(updatedCar.getMileage());
        existingCar.setModel(updatedCar.getModel());
        existingCar.setRentalRatePerDay(updatedCar.getRentalRatePerDay());

        log.info("Car details updated for registration number: {}", registrationNumber);
        return carRepository.save(existingCar);
    }

    @Override
    public Car updateCarStatus(String registrationNumber, CarStatus status) {

        log.info("Updating status for car {} to {}", registrationNumber, status);

        if (status == null) {
            log.warn("Invalid car status provided for registration number: {}", registrationNumber);
            throw new InvalidEntityException("Car status cannot be null");
        }

        Car car = getCarByRegistrationNumber(registrationNumber);
        car.setStatus(status);

        log.info("Car status updated successfully for {} to {}",
                registrationNumber, status);

        return carRepository.save(car);
    }

    @Override
    public Car updateMileageAfterRental(String registrationNumber, Double newMileage) {

        log.info("Updating mileage for car {} after rental. New mileage: {}",
                registrationNumber, newMileage);

        Car car = getCarByRegistrationNumber(registrationNumber);
        car.setMileage(newMileage);
        car.setRentalCount(car.getRentalCount() + 1);

        if (needsMaintenance(car)) {
            log.warn("Car {} requires maintenance. Status changed to MAINTENANCE",
                    registrationNumber);
            car.setStatus(CarStatus.MAINTENANCE);
        }

        return carRepository.save(car);
    }

    /* ---------------- FETCH ---------------- */
    @Override
    public List<Car> getAllCars() {
        log.info("Fetching all cars");
        return carRepository.findAll();
    }

    @Override
    public Car getCarByRegistrationNumber(String registrationNumber) {

        log.debug("Fetching car with registration number: {}", registrationNumber);

        return carRepository.findById(registrationNumber)
                .orElseThrow(() -> {
                    log.error("Car not found with registration number: {}", registrationNumber);
                    return new InvalidEntityException(
                            "Car not found with registration number: " + registrationNumber);
                });
    }

    @Override
    public List<Car> getCarsByModel(String model) {
        log.info("Fetching cars by model: {}", model);
        return carRepository.findByModel(model);
    }

    @Override
    public List<Car> getCarsByCategory(String category) {
        log.info("Fetching cars by category: {}", category);
        return carRepository.findByCategory(category);
    }

    @Override
    public List<Car> getCarsByStatus(CarStatus status) {
        log.info("Fetching cars with status: {}", status);
        return carRepository.findByStatus(status);
    }

    @Override
    public List<Car> getAvailableCars() {
        log.info("Fetching available cars");
        return carRepository.findByStatus(CarStatus.AVAILABLE);
    }

    @Override
    public List<Car> getCarsRequiringMaintenance() {
        log.info("Fetching cars requiring maintenance");
        return carRepository.findAll().stream()
                .filter(this::needsMaintenance)
                .toList();
    }

    /* ---------------- DELETE ---------------- */
    @Override
    public void deleteCar(String registrationNumber) {
        log.info("Request received to delete car: {}", registrationNumber);
        if (!carRepository.existsById(registrationNumber)) {
            throw new InvalidEntityException("Car with Registration Number " + registrationNumber + " not found");
        }
        carRepository.deleteById(registrationNumber);
        log.info("Car {} deleted successfully", registrationNumber);
    }

    /* ---------------- BUSINESS RULES ---------------- */
    @Override
    public boolean needsMaintenance(Car car) {

        // Rule 1: 5,000 km since last service
        boolean mileageExceeded =
                car.getLastServiceMileage() != null &&
                car.getMileage() != null &&
                (car.getMileage() - car.getLastServiceMileage()) >= 5_000;

        // Rule 2: 90 days (3 months) since last service
        boolean serviceOverdue =
                car.getLastServiceDate() != null &&
                ChronoUnit.DAYS.between(car.getLastServiceDate(), LocalDate.now()) >= 90;

        // Rule 3: 20 rentals since last service
        boolean rentalCountExceeded =
                car.getRentalCount() != null &&
                car.getRentalCount() >= 20;

        return mileageExceeded || serviceOverdue || rentalCountExceeded;
    }
}