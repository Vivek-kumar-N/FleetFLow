package com.carcaddy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @NotNull
    @Column(name = "account_active", nullable = false)
    private Boolean accountActive;

    @NotBlank
    @Column(name = "account_type")
    private String accountType;

    @NotBlank
    @Pattern(
        regexp = "^[6-9][0-9]{9}$",
        message = "Contact number must be a valid 10-digit Indian number"
    )
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(name = "designation", nullable = false)
    private String designation;

    @NotBlank
    @Email
    @Column(name = "email_id", nullable = false, unique = true)
    private String emailId;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @NotNull
    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    /* --------------------------------------------------
       Employee → AppUser relationship (required)
       -------------------------------------------------- */
    @OneToOne
    @JoinColumn(name = "user_id")
    private AppUser appUser;
}