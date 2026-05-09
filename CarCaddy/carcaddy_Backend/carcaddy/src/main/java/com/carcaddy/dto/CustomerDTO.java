package com.carcaddy.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private String customerName;
    private String contactNumber;
    private String drivingLicense;
    private String occupation;
    private String address;
    private String emailId;
}
