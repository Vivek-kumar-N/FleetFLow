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
