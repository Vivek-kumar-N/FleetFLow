package com.carcaddy.dto;

import com.carcaddy.entity.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceDto {

    private Long maintenanceId;

    //  Required field
    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    //  Enum required
    @NotNull(message = "Maintenance type is required")
    private MaintenanceType maintenanceType;

    private LocalDate scheduledDate;

    private LocalDate completedDate;

    @Size(max = 255, message = "Description should not exceed 255 characters")
    private String description;

    //  Same as entity
    @Positive(message = "Cost must be positive")
    private double cost;

    @Size(max = 100, message = "PerformedBy should not exceed 100 characters")
    private String performedBy;

    
    private MaintenanceStatus status;
}
