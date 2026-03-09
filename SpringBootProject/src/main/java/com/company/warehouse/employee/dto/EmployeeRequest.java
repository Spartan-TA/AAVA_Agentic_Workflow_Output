package com.company.warehouse.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for creating/updating Employee.
 */
@Data
public class EmployeeRequest {
    @NotBlank
    private String badgeId;
    @NotBlank
    private String name;
    @NotBlank
    private String role;
    @NotBlank
    private String department;
    @NotBlank
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    private String status;
}
