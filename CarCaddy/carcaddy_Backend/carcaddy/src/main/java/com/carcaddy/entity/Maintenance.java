package com.carcaddy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "maintenance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maintenanceId;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    private MaintenanceType maintenanceType; 

    private LocalDate scheduledDate;

    private LocalDate completedDate;

    private String description;

    @Positive(message = "Cost must be positive")
    private double cost;

    private String performedBy; 

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status; 
}
