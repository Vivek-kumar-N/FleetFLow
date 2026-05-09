package com.carcaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnCarRequest {
    
    
    private Double mileageAtReturn;
    private boolean damaged;
    private String damageNotes;

}
