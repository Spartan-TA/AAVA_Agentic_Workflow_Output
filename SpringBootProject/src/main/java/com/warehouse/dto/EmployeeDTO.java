package com.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests and responses.
 */
@Data
public class EmployeeDTO {
    @Schema(description = "Full name of the employee", example = "Jane Doe")
    @NotBlank
    private String name;

    @Schema(description = "Unique badge ID", example = "BADGE12345")
    @NotBlank
    private String badgeId;

    @Schema(description = "Role of the employee", example = "WORKER")
    @NotBlank
    private String role;

    @Schema(description = "Department name", example = "Shipping")
    @NotBlank
    private String department;

    @Schema(description = "Shift group", example = "A")
    @NotBlank
    private String shiftGroup;

    @Schema(description = "Hire date", example = "2023-01-15")
    @PastOrPresent
    private LocalDate hireDate;

    @Schema(description = "Employment status", example = "ACTIVE")
    @NotBlank
    private String status;
}
