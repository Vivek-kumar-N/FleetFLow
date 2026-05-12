package com.carcaddy.service.impl;

import com.carcaddy.dto.MaintenanceDto;
import com.carcaddy.entity.*;
import com.carcaddy.exception.InvalidEntityException;
import com.carcaddy.repository.CarRepository;
import com.carcaddy.repository.MaintenanceRepository;
import com.carcaddy.service.IMaintenanceService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class MaintenanceServiceImpl implements IMaintenanceService {

    private final MaintenanceRepository repo;
    private final CarRepository carRepository;

    public MaintenanceServiceImpl(MaintenanceRepository repo,
                                  CarRepository carRepository) {
        this.repo = repo;
        this.carRepository = carRepository;
    }

    // PURE CONVERSION (NO BUSINESS LOGIC)
    private Maintenance convertToEntity(MaintenanceDto dto, Car car) {

        Maintenance m = new Maintenance();

        m.setMaintenanceId(dto.getMaintenanceId());
        m.setMaintenanceType(dto.getMaintenanceType());
        m.setScheduledDate(dto.getScheduledDate());
        m.setCompletedDate(dto.getCompletedDate());
        m.setDescription(dto.getDescription());
        m.setCost(dto.getCost());
        m.setPerformedBy(dto.getPerformedBy());
        m.setStatus(dto.getStatus());
        m.setCar(car);

        return m;
    }

    private MaintenanceDto convertToDTO(Maintenance m) {

        MaintenanceDto dto = new MaintenanceDto();

        dto.setMaintenanceId(m.getMaintenanceId());
        dto.setMaintenanceType(m.getMaintenanceType());
        dto.setScheduledDate(m.getScheduledDate());
        dto.setCompletedDate(m.getCompletedDate());
        dto.setDescription(m.getDescription());
        dto.setCost(m.getCost());
        dto.setPerformedBy(m.getPerformedBy());
        dto.setStatus(m.getStatus());

        if (m.getCar() != null) {
            dto.setRegistrationNumber(m.getCar().getRegistrationNumber());
        }

        return dto;
    }

    //  BUSINESS VALIDATION: CHECK CAR EXISTS
    private Car getCarOrThrow(String regNumber) {

        log.info("Checking if car exists: {}", regNumber);

        return carRepository.findById(regNumber)
                .orElseThrow(() ->
                        new InvalidEntityException(
                                "Car with Registration Number " + regNumber + " not found"));
    }

    //  BUSINESS VALIDATION: CHECK EXISTING MAINTENANCE
    private void validateMaintenanceExists(String regNumber) {

        log.info("Checking existing maintenance records for car: {}", regNumber);

        List<Maintenance> list = repo.findByRegistrationNumber(regNumber);

        if (!list.isEmpty()) {

            log.info("Maintenance already exists for car: {}", regNumber);

            throw new InvalidEntityException(
                    "Maintenance already exists for car with Registration Number: " + regNumber
            );
        }
    }

    // ADD MAINTENANCE
    @Override
    public MaintenanceDto addMaintenance(MaintenanceDto dto) {

        log.info("Adding maintenance for car: {}", dto.getRegistrationNumber());

        Car car = getCarOrThrow(dto.getRegistrationNumber());

        
        validateMaintenanceExists(dto.getRegistrationNumber());

        Maintenance m = convertToEntity(dto, car);

        return convertToDTO(repo.save(m));
    }

    @Override
    public MaintenanceDto scheduleRoutineMaintenance(MaintenanceDto dto) {
    
        log.info("Scheduling routine maintenance for car: {}", dto.getRegistrationNumber());
    
        Car car = getCarOrThrow(dto.getRegistrationNumber());
    
        boolean mileageDue = false;
        boolean timeDue = false;
        boolean usageDue = false;
    
        if (car.getMileage() != null && car.getLastServiceMileage() != null) {
            mileageDue = (car.getMileage() - car.getLastServiceMileage()) >= 5000;
        }
    
        if (car.getLastServiceDate() != null) {
            timeDue = car.getLastServiceDate().plusDays(90).isBefore(LocalDate.now());
        }
    
        if (car.getRentalCount() != null) {
            usageDue = car.getRentalCount() >= 20;
        }
    
        
        if (!(mileageDue || timeDue || usageDue)) {
            log.info("No conditions met, but scheduling routine maintenance anyway");
        }
    
        dto.setMaintenanceType(MaintenanceType.ROUTINE);
        dto.setStatus(MaintenanceStatus.SCHEDULED);
    
        //  update car status
        car.setStatus(CarStatus.MAINTENANCE);
        carRepository.save(car);
    
        
        Maintenance m = convertToEntity(dto, car);
    
        return convertToDTO(repo.save(m));
    }
    

    @Override
    public MaintenanceDto addEmergencyMaintenance(MaintenanceDto dto) {

        log.info("Adding emergency maintenance for car: {}", dto.getRegistrationNumber());

        dto.setMaintenanceType(MaintenanceType.EMERGENCY);
        dto.setStatus(MaintenanceStatus.IN_PROGRESS);

        return addMaintenance(dto);
    }

    @Override
    public MaintenanceDto updateStatus(Long id, MaintenanceStatus status) {
    
        log.info("Updating maintenance ID {} to status {}", id, status);
    
        Maintenance m = repo.findById(id)
                .orElseThrow(() ->
                        new InvalidEntityException("Maintenance ID " + id + " not found"));
    
        m.setStatus(status);
    
        if (status == MaintenanceStatus.COMPLETED) {
    
            log.info("Maintenance completed, updating completedDate and car status");
    
            m.setCompletedDate(LocalDate.now());
    
            //  fetch explicitly
            String reg = m.getCar().getRegistrationNumber();
            Car car = carRepository.findById(reg)
                    .orElseThrow(() ->
                            new InvalidEntityException("Car with Registration Number " + reg + " not found"));
    
            car.setStatus(CarStatus.AVAILABLE);
            carRepository.save(car);
        }
    
        return convertToDTO(repo.save(m));
    }

    @Override
    public List<MaintenanceDto> getAll() {

        log.info("Fetching all maintenance records");

        return repo.findAll().stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<MaintenanceDto> getByRegNumber(String regNumber) {

        log.info("Fetching maintenance for car: {}", regNumber);

        return repo.findByRegistrationNumber(regNumber)
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<MaintenanceDto> getByType(MaintenanceType type) {

        log.info("Fetching maintenance by type: {}", type);

        return repo.findByMaintenanceType(type)
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<MaintenanceDto> getByStatus(MaintenanceStatus status) {

        log.info("Fetching maintenance by status: {}", status);

        return repo.findByStatus(status)
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<MaintenanceDto> getUpcomingMaintenance() {

        log.info("Fetching upcoming maintenance");

        return repo.findUpcoming()
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<MaintenanceDto> getOverdueMaintenance() {

        log.info("Fetching overdue maintenance");

        return repo.findOverdue()
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public double getTotalCostByCar(String regNumber) {

        log.info("Calculating total maintenance cost for car: {}", regNumber);

        Double total = repo.getMaintenanceCostByCar(regNumber);
        return total != null ? total : 0.0;
    }

    @Override
    public List<MaintenanceDto> getByDateRange(LocalDate start, LocalDate end) {

        log.info("Fetching maintenance between {} and {}", start, end);

        return repo.findByScheduledDateBetween(start, end)
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public void delete(Long id) {

        log.info("Deleting maintenance ID {}", id);

        if (!repo.existsById(id)) {
            throw new InvalidEntityException(
                    "Maintenance ID " + id + " not found");
        }

        repo.deleteById(id);
    }

    @Override
    public MaintenanceDto getById(Long id) {

        log.info("Fetching maintenance ID {}", id);

        return convertToDTO(
                repo.findById(id)
                        .orElseThrow(() ->
                                new InvalidEntityException(
                                        "Maintenance ID " + id + " not found"))
        );
    }
}
