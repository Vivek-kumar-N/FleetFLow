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
public class ModifyBookingRequest {

    
    private LocalDate startDate;
    private LocalDate endDate;
    private String category; // optional
    private String model;


}
