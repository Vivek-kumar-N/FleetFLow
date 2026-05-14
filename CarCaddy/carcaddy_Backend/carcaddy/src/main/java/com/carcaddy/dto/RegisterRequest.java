package com.carcaddy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String role; // ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_CUSTOMER
    private String securityQuestion;
    private String securityAnswer;

    //for customer

    private String customerName;
    private String emailId;
    private String contactNumber;
    private String drivingLicense;
    private String address;
    private String occupation;
}

