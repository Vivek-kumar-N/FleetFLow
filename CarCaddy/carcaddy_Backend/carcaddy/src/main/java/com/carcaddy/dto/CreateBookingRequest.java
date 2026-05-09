package com.carcaddy.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    private String customerId;
    private String category;
    private String model;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer passengerCount;


}
