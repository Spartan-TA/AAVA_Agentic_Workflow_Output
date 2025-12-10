package com.warehouse.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API responses.
 */
public class EmployeeDTO {
    private Long id;
    @NotBlank
    @Size(max = 100)
    private String name;
    @NotBlank
    @Size(max = 20)
    private String badgeId;
    @NotBlank
    @Size(max = 50)
    private String role;
    @NotBlank
    @Size(max = 50)
    private String department;
    @Size(max = 50)
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    @Size(max = 20)
    private String status;
    // Getters and setters omitted for brevity
}
