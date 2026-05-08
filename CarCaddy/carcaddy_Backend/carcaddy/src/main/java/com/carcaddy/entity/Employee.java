package com.carcaddy.entity;

import jakarta.persistence.*;
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
@Table(
    name = "employee",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email_id")
    }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "account_active", nullable = false)
    private Boolean accountActive;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "designation", nullable = false)
    private String designation;

    @Column(name = "email_id", nullable = false, unique = true)
    private String emailId;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin;

    @Column(name = "password", nullable = false)
    private String password;

    
}