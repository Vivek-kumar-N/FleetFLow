package com.carcaddy.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "customer")
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
    @Column(name = "driving_license", nullable = false, unique = true)
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
    @Column(name = "email_id", nullable = false, unique = true)
    private String emailId;

    @Column(name = "loyalty_points", nullable = true)
    private Integer loyaltyPoints = 0;

    @Column(name = "blacklisted", nullable = true)
    private Boolean blacklisted = false;

    @Column(name = "blacklist_reason", nullable = true)
    private String blacklistReason;


    //Customer–Booking relationship
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnore
    private List<Booking> bookings;

    //Customer–AppUser relationship
    @OneToOne
    @JoinColumn(name = "user_id")
    private AppUser appUser;


   
}


