package com.carcaddy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_email", columnNames = "email_id"),
        @UniqueConstraint(name = "uk_customer_dl", columnNames = "driving_license")
})
public class Customer {

    @Id
    @Column(name = "customer_id", length = 255)
    private String customerId;

    @NotBlank(message = "customerName is required")
    @Size(min = 2, max = 255, message = "customerName must be between 2 and 255 characters")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotBlank(message = "contactNumber is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "contactNumber must be 10 digits")
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @NotBlank(message = "drivingLicense is required")
    @Size(min = 5, max = 255, message = "drivingLicense must be between 5 and 255 characters")
    @Column(name = "driving_license", nullable = false)
    private String drivingLicense;

    @Size(max = 255, message = "occupation must be <= 255 characters")
    @Column(name = "occupation", nullable = true)
    private String occupation;

    @NotBlank(message = "address is required")
    @Size(min = 5, max = 255, message = "address must be between 5 and 255 characters")
    @Column(name = "address", nullable = false)
    private String address;

    @NotBlank(message = "emailId is required")
    @Email(message = "emailId must be a valid email")
    @Column(name = "email_id", nullable = false)
    private String emailId;

    @Column(name = "loyalty_points", nullable = true)
    private Integer loyaltyPoints = 0;

    @Column(name = "blacklisted", nullable = true)
    private Boolean blacklisted = false;

    @Column(name = "blacklist_reason", nullable = true)
    private String blacklistReason;

    // @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    // @JsonManagedReference
    // private List<Booking> bookings;

    public Customer() {
    }

    // Getters & Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public Boolean getBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(Boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public String getBlacklistReason() {
        return blacklistReason;
    }

    public void setBlacklistReason(String blacklistReason) {
        this.blacklistReason = blacklistReason;
    }
}


