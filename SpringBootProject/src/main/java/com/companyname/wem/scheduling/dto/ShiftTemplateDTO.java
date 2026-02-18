package com.companyname.wem.scheduling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class ShiftTemplateDTO {
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotNull
    private LocalTime startTime;
    
    @NotNull
    private LocalTime endTime;
    
    private boolean recurring;
    private String recurrencePattern;
}
