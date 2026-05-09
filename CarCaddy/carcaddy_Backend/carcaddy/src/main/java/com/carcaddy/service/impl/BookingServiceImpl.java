package com.carcaddy.service.impl;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carcaddy.dto.CreateBookingRequest;
import com.carcaddy.dto.ModifyBookingRequest;
import com.carcaddy.dto.ReturnCarRequest;
import com.carcaddy.entity.Booking;
import com.carcaddy.entity.BookingStatus;
import com.carcaddy.entity.Car;
import com.carcaddy.entity.CarStatus;
import com.carcaddy.entity.Customer;
import com.carcaddy.repository.BookingRepository;
import com.carcaddy.repository.CarRepository;
import com.carcaddy.repository.CustomerRepository;
import com.carcaddy.service.IBookingService;

@Service
public class BookingServiceImpl implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    //  1. CREATE BOOKING
    @Override
    public Booking createBooking(CreateBookingRequest request) {

    System.out.println(" CreateBooking API START");

    Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        
    if (Boolean.TRUE.equals(customer.getBlacklisted())) {
        throw new RuntimeException(
            "Customer is blacklisted: " + customer.getBlacklistReason()
        );
    }


    System.out.println(" Customer found: " + customer.getCustomerId());

    List<Car> cars = carRepository.findByModelAndStatus(
            request.getModel(), CarStatus.AVAILABLE
    );

    System.out.println(" Cars fetched: " + cars.size());

    for (Car c : cars) {
        System.out.println("Car: " + c.getRegistrationNumber()
                + ", Model: " + c.getModel()
                + ", Status: " + c.getStatus());
    }

    List<Car> availableCars = new ArrayList<>();

    for (Car car : cars) {

        boolean available = checkCarAvailability(
                car.getRegistrationNumber(),
                request.getStartDate(),
                request.getEndDate()
        );

        System.out.println(" Checking availability for "
                + car.getRegistrationNumber()
                + " → " + available);

        if (available) {
            availableCars.add(car);
        }
    }

    System.out.println(" Final available cars: " + availableCars.size());

    if (availableCars.isEmpty()) {
        throw new RuntimeException("No available cars");
    }

    Car selectedCar = availableCars.get(new Random().nextInt(availableCars.size()));

    System.out.println(" Selected car: " + selectedCar.getRegistrationNumber());

    long days = ChronoUnit.DAYS.between(
            request.getStartDate(), request.getEndDate()
    );

    if (days == 0) days = 1;

    double baseFare = days * selectedCar.getRentalRatePerDay();

    double discount = 0;
    if (customer.getLoyaltyPoints() >= 500) {
        discount = selectedCar.getRentalRatePerDay();
    } else if (customer.getLoyaltyPoints() >= 100) {
        discount = baseFare * 0.05;
    }

    System.out.println(" Base fare: " + baseFare);
    System.out.println(" Discount: " + discount);

    Booking booking = new Booking();
    booking.setCustomer(customer);
    booking.setCar(selectedCar);
    booking.setStartDate(request.getStartDate());
    booking.setEndDate(request.getEndDate());
    booking.setPassengerCount(request.getPassengerCount());
    booking.setMileageAtStart(selectedCar.getMileage());
    booking.setDiscount(discount);
    booking.setTotalFare(baseFare - discount);
    booking.setBookingStatus(BookingStatus.CONFIRMED);

    Booking saved = bookingRepository.save(booking);

    selectedCar.setStatus(CarStatus.RENTED);
    selectedCar.setRentalCount(selectedCar.getRentalCount() + 1);
    carRepository.save(selectedCar);

    System.out.println(" Booking CREATED successfully: " + saved.getBookingId());

    return saved;
}


    //  2. MODIFY BOOKING
    // @Override
    // public Booking modifyBooking(Long bookingId, ModifyBookingRequest request) {

    //     Booking booking = bookingRepository.findById(bookingId)
    //             .orElseThrow(() -> new RuntimeException("Booking not found"));

    //     if (booking.getBookingStatus() == BookingStatus.CANCELLED ||
    //         booking.getBookingStatus() == BookingStatus.COMPLETED) {
    //         throw new RuntimeException("Cannot modify this booking");
    //     }

    //     LocalDate newStart = request.getStartDate() != null ?
    //             request.getStartDate() : booking.getStartDate();

    //     LocalDate newEnd = request.getEndDate() != null ?
    //             request.getEndDate() : booking.getEndDate();

    //     //  availability check excluding same booking
    //     List<Booking> conflicts = bookingRepository.findOverlappingBookingsExcludingId(
    //             booking.getCar().getRegistrationNumber(),
    //             bookingId,
    //             newStart,
    //             newEnd
    //             //check1
    //     );

    //     if (!conflicts.isEmpty()) {
    //         throw new RuntimeException("Dates not available");
    //     }

    //     booking.setStartDate(newStart);
    //     booking.setEndDate(newEnd);

    //     //  category change → reallocate car
    //     if (request.getCategory() != null) {

    //         List<Car> cars = carRepository.findByCategoryAndStatus(
    //                 request.getCategory(), CarStatus.AVAILABLE
    //         );

    //         Car newCar = cars.get(0); // simplified

    //         booking.getCar().setStatus(CarStatus.AVAILABLE);
    //         booking.setCar(newCar);
    //         newCar.setStatus(CarStatus.RENTED);

    //         carRepository.save(newCar);
    //     }

    //     booking.setBookingStatus(BookingStatus.MODIFIED);

    //     return bookingRepository.save(booking);
    // }

    //2. Modify Booking

    @Override
    public Booking modifyBooking(Long bookingId, ModifyBookingRequest request) {

    //  1. Fetch booking
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

    //  2. Validate status
    if (booking.getBookingStatus() == BookingStatus.CANCELLED ||
        booking.getBookingStatus() == BookingStatus.COMPLETED) {
        throw new RuntimeException("Cannot modify this booking");
    }

    //  3. Handle new dates
    LocalDate newStart = request.getStartDate() != null
            ? request.getStartDate()
            : booking.getStartDate();

    LocalDate newEnd = request.getEndDate() != null
            ? request.getEndDate()
            : booking.getEndDate();

    //  4. Check availability for current car (exclude itself)
    List<Booking> conflicts = bookingRepository.findOverlappingBookingsExcludingId(
            booking.getCar().getRegistrationNumber(),
            bookingId,
            newStart,
            newEnd
    );

    if (!conflicts.isEmpty()) {
        throw new RuntimeException("Dates not available for current car");
    }

    //  5. Update dates
    booking.setStartDate(newStart);
    booking.setEndDate(newEnd);

    //  6. Handle car change (MODEL based)
    if (request.getModel() != null) {

        Car oldCar = booking.getCar();

        List<Car> cars = carRepository.findByModelAndStatus(
                request.getModel(), CarStatus.AVAILABLE
        );

        if (cars.isEmpty()) {
            throw new RuntimeException("No cars available for selected model");
        }

        //  filter available cars (no overlap)
        List<Car> availableCars = new ArrayList<>();

        for (Car car : cars) {
            boolean available = checkCarAvailability(
                    car.getRegistrationNumber(),
                    newStart,
                    newEnd
            );

            if (available) {
                availableCars.add(car);
            }
        }

        if (availableCars.isEmpty()) {
            throw new RuntimeException("No cars available after filtering");
        }

        //  randomly pick new car
        Car newCar = availableCars.get(new Random().nextInt(availableCars.size()));

        //  free old car
        oldCar.setStatus(CarStatus.AVAILABLE);
        carRepository.save(oldCar);

        //  assign new car
        booking.setCar(newCar);

        newCar.setStatus(CarStatus.RENTED);
        newCar.setRentalCount(newCar.getRentalCount() + 1);
        carRepository.save(newCar);
    }

    //  7. Recalculate fare
    long days = ChronoUnit.DAYS.between(newStart, newEnd);
    if (days == 0) days = 1;

    double baseFare = days * booking.getCar().getRentalRatePerDay();

    Customer customer = booking.getCustomer();

    double discount = 0;
    if (customer.getLoyaltyPoints() >= 500) {
        discount = booking.getCar().getRentalRatePerDay();
    } else if (customer.getLoyaltyPoints() >= 100) {
        discount = baseFare * 0.05;
    }

    booking.setTotalFare(baseFare - discount);
    booking.setDiscount(discount);

    //  8. Update status
    booking.setBookingStatus(BookingStatus.MODIFIED);

    //  9. Save and return
    return bookingRepository.save(booking);
    }




    //  3. CANCEL BOOKING
    @Override
    public String cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        booking.setBookingStatus(BookingStatus.CANCELLED);

        Car car = booking.getCar();
        car.setStatus(CarStatus.AVAILABLE);

        carRepository.save(car);
        bookingRepository.save(booking);

        return "Booking cancelled successfully";
    }

    //  4. RETURN CAR
    @Override
    public Booking returnCar(Long bookingId, ReturnCarRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setReturnDate(LocalDate.now());
        booking.setMileageAtReturn(request.getMileageAtReturn());

        Car car = booking.getCar();
        car.setMileage(request.getMileageAtReturn());
        car.setStatus(CarStatus.AVAILABLE);

        //  maintenance check
        if (request.isDamaged() || car.getMileage() >= 5000) {
            car.setStatus(CarStatus.MAINTENANCE);
        }

        //  loyalty update
        Customer customer = booking.getCustomer();
        int points = (int)(booking.getTotalFare() / 100) * 10;
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);

        customerRepository.save(customer);
        carRepository.save(car);

        return bookingRepository.save(booking);
    }

    //  5. CHECK AVAILABILITY
    @Override
    public boolean checkCarAvailability(String regNo, LocalDate start, LocalDate end) {
        return bookingRepository.findOverlappingBookings(regNo, start, end).isEmpty();
    }

    //  VIEW METHODS

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    @Override
    public List<Booking> getBookingsByCustomer(String customerId) {
        return bookingRepository.findByCustomer_CustomerId(customerId);
    }

    @Override
    public List<Booking> getBookingsByCar(String regNo) {
        return bookingRepository.findByCar_RegistrationNumber(regNo);
    }

    @Override
    public List<Booking> getActiveBookings() {
        return bookingRepository.findByBookingStatusIn(
                List.of(BookingStatus.CONFIRMED, BookingStatus.ACTIVE)
        );
    }

    @Override
    public List<Booking> getCompletedBookings() {
        return bookingRepository.findByBookingStatus(BookingStatus.COMPLETED);
    }
}