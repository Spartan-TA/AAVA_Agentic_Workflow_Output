package com.warehouse.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for Employee API responses and requests.
 */
@Schema(description = "Employee data transfer object")
public class EmployeeDto {
    @Schema(example = "12345")
    private Long id;

    @NotBlank
    @Schema(example = "BADGE001")
    private String badgeId;

    @NotBlank
    @Schema(example = "John Doe")
    private String name;

    @NotBlank
    @Schema(example = "WORKER")
    private String role;

    @NotBlank
    @Schema(example = "Shipping")
    private String department;

    @NotBlank
    @Schema(example = "A")
    private String shiftGroup;

    @NotNull
    @Schema(example = "2023-01-01")
    private LocalDate hireDate;

    @NotBlank
    @Schema(example = "ACTIVE")
    private String status;

    // Getters and setters omitted for brevity
    // ...
}
