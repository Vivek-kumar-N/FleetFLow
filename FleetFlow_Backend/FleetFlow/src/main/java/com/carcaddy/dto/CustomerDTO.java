package com.carcaddy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerDTO {

    @NotBlank(message = "customerName is required")
    @Size(min = 2, max = 255, message = "customerName must be between 2 and 255 characters")
    private String customerName;

    @NotBlank(message = "contactNumber is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "contactNumber must be 10 digits")
    private String contactNumber;

    @NotBlank(message = "drivingLicense is required")
    @Size(min = 5, max = 255, message = "drivingLicense must be between 5 and 255 characters")
    private String drivingLicense;

    @Size(max = 255, message = "occupation must be <= 255 characters")
    private String occupation;

    @NotBlank(message = "address is required")
    @Size(min = 5, max = 255, message = "address must be between 5 and 255 characters")
    private String address;

    @NotBlank(message = "emailId is required")
    @Email(message = "emailId must be a valid email")
    private String emailId;
}
