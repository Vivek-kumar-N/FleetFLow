package com.carcaddy.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Passenger count is required")
    @Min(value = 1, message = "Minimum 1 passenger required")
    @Max(value = 10, message = "Maximum 10 passengers allowed")
    private Integer passengerCount;


}
