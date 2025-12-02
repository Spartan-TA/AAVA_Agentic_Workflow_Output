package com.warehouse.ems.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests and responses.
 */
public class EmployeeDTO {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String badgeId;

    @NotBlank
    private String role;

    @NotBlank
    private String department;

    @NotBlank
    private String shiftGroup;

    @PastOrPresent
    @NotNull
    private LocalDate hireDate;

    @NotBlank
    private String status;

    // Getters and setters omitted for brevity
}
