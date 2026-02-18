package com.companyname.wem.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
lombok.Data;
import java.time.LocalDate;

@Data
public class CertificationDTO {
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotNull
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    @NotNull
    private Long employeeId;
    
    private String documentUrl;
}
