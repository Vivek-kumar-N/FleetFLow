package com.carcaddy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnCarRequest {
    
    
    @NotNull(message = "Mileage at return is required")
    @DecimalMin(value = "0.0", message = "Mileage cannot be negative")
    private Double mileageAtReturn;

    private boolean damaged;

    @Size(max = 255, message = "Damage notes too long")
    private String damageNotes;

}
