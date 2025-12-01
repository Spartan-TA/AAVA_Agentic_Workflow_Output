package com.warehouse.ems.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for Employee API requests and responses.
 */
@Data
public class EmployeeDto {
    @Schema(description = "Employee name", example = "John Doe")
    @NotBlank
    private String name;

    @Schema(description = "Unique badge ID", example = "B12345")
    @NotBlank
    private String badgeId;

    @Schema(description = "Role", example = "WORKER")
    @NotBlank
    private String role;

    @Schema(description = "Department", example = "Shipping")
    @NotBlank
    private String department;

    @Schema(description = "Shift group", example = "A")
    private String shiftGroup;

    @Schema(description = "Hire date", example = "2023-01-15")
    @NotNull
    private LocalDate hireDate;

    @Schema(description = "Status", example = "ACTIVE")
    @NotBlank
    private String status;
}
