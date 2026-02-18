package com.companyname.wem.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftAssignmentDTO {
    private Long id;
    
    @NotNull
    private Long employeeId;
    
    @NotNull
    private Long shiftTemplateId;
    
    @NotNull
    private LocalDate date;
    
    private boolean overtime;
}
