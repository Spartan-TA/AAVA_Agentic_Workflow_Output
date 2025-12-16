package com.warehouse.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for Employee create/update requests.
 */
public class EmployeeRequestDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotBlank
    private String role;
    @NotBlank
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    private String status;
    // Getters and setters omitted for brevity
}
