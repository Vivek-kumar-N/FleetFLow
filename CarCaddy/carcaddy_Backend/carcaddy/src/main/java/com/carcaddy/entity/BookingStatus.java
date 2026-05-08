package com.carcaddy.entity;

public enum BookingStatus {

    CONFIRMED,   // booking created successfully
    ACTIVE,      // car picked up, rental ongoing
    COMPLETED,   // car returned
    CANCELLED,   // cancelled by user
    MODIFIED     

}
