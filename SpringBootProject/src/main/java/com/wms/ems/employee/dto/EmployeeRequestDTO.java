package com.wms.ems.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for Employee create/update requests.
 */
@Data
public class EmployeeRequestDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotBlank
    private String role;
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    private String status;
}
