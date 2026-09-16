package com.carcaddy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @NotNull(message = "Maintenance type is required")
    @Enumerated(EnumType.STRING)
    private MaintenanceType maintenanceType;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    private LocalDate completedDate;

    private String description;

    @PositiveOrZero(message = "Cost must be zero or positive")
    private double cost;

    private String performedBy;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "registration_number")
    private Car car;

    @jakarta.persistence.PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
