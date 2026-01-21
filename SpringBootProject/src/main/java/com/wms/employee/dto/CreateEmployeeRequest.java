package com.wms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for creating a new Employee.
 */
public class CreateEmployeeRequest {
    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotBlank
    @Size(max = 64)
    private String role;

    @NotBlank
    @Size(max = 64)
    private String department;

    @Size(max = 64)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 32)
    private String status;

    // Getters and setters omitted for brevity
}
