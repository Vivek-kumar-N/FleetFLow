package com.carcaddy.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
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

    @Size(max = 20, message = "Category too long")
    private String category;

    @Size(max = 50, message = "Model name too long")
    private String model;



}
