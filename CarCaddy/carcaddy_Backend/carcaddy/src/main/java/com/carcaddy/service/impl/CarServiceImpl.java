package com.carcaddy.service.impl;




import com.carcaddy.entity.Car;
import com.carcaddy.entity.CarStatus;
import com.carcaddy.repository.CarRepository;
import com.carcaddy.service.ICarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements ICarService {

    private final CarRepository carRepository;

    /* ---------------- ADD ---------------- */
    @Override
    public Car addCar(Car car) {
        car.setRentalCount(0);
        car.setStatus(CarStatus.AVAILABLE);
        return carRepository.save(car);
    }

    /* ---------------- UPDATE ---------------- */
    @Override
    public Car updateCar(String registrationNumber, Car updatedCar) {
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

        return carRepository.save(existingCar);
    }

    @Override
    public Car updateCarStatus(String registrationNumber, CarStatus status) {
        Car car = getCarByRegistrationNumber(registrationNumber);
        car.setStatus(status);
        return carRepository.save(car);
    }

    @Override
    public Car updateMileageAfterRental(String registrationNumber, Double newMileage) {
        Car car = getCarByRegistrationNumber(registrationNumber);

        car.setMileage(newMileage);
        car.setRentalCount(car.getRentalCount() + 1);

        if (needsMaintenance(car)) {
            car.setStatus(CarStatus.MAINTENANCE);
        }

        return carRepository.save(car);
    }

    /* ---------------- FETCH ---------------- */
    @Override
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @Override
    public Car getCarByRegistrationNumber(String registrationNumber) {
        return carRepository.findById(registrationNumber)
                .orElseThrow(() -> new RuntimeException("Car not found: " + registrationNumber));
    }

    @Override
    public List<Car> getCarsByModel(String model) {
        return carRepository.findByModel(model);
    }

    @Override
    public List<Car> getCarsByCategory(String category) {
        return carRepository.findByCategory(category);
    }

    @Override
    public List<Car> getCarsByStatus(CarStatus status) {
        return carRepository.findByStatus(status);
    }

    @Override
    public List<Car> getAvailableCars() {
        return carRepository.findByStatus(CarStatus.AVAILABLE);
    }

    @Override
    public List<Car> getCarsRequiringMaintenance() {
        return carRepository.findAll()
                .stream()
                .filter(this::needsMaintenance)
                .toList();
    }

    /* ---------------- BUSINESS RULES ---------------- */
    @Override
    public boolean needsMaintenance(Car car) {

        // Rule 1: Mileage difference > 10,000 km
        boolean mileageExceeded =
                car.getLastServiceMileage() != null &&
                car.getMileage() != null &&
                (car.getMileage() - car.getLastServiceMileage()) >= 10000;

        // Rule 2: Last service > 6 months ago
        boolean serviceOverdue =
                car.getLastServiceDate() != null &&
                ChronoUnit.MONTHS.between(car.getLastServiceDate(), LocalDate.now()) >= 6;

        return mileageExceeded || serviceOverdue;
    }
}