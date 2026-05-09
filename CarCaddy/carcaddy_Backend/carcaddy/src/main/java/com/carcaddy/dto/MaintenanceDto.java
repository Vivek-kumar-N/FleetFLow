package com.carcaddy.dto;

import com.carcaddy.entity.*;
import java.time.LocalDate;

public class MaintenanceDto {

    private Long maintenanceId;
    private String registrationNumber;
    private MaintenanceType maintenanceType;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String description;
    private double cost;
    private String performedBy;
    private MaintenanceStatus status;


    
    public Long getMaintenanceId() {
        return maintenanceId;
    }
    public void setMaintenanceId(Long maintenanceId) {
        this.maintenanceId = maintenanceId;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    public MaintenanceType getMaintenanceType() {
        return maintenanceType;
    }
    public void setMaintenanceType(MaintenanceType maintenanceType) {
        this.maintenanceType = maintenanceType;
    }
    public LocalDate getScheduledDate() {
        return scheduledDate;
    }
    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    public LocalDate getCompletedDate() {
        return completedDate;
    }
    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public String getPerformedBy() {
        return performedBy;
    }
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    public MaintenanceStatus getStatus() {
        return status;
    }
    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }

    
}
