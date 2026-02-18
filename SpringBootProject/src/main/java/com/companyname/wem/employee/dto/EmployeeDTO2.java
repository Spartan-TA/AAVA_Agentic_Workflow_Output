package com.companyname.wem.employee.dto;

import com.companyname.wem.employee.domain.Role;
import com.companyname.wem.employee.domain.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Badge ID is required")
    private String badgeId;
    
    @NotNull(message = "Role is required")
    private Role role;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    private String shiftGroup;
    
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @NotNull(message = "Status is required")
    private Status status;
}
