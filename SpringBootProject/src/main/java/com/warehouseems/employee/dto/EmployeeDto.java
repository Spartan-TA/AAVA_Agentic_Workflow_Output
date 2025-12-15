package com.warehouseems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests/responses.
 * Includes validation annotations for input validation.
 */
@Data
@Schema(description = "Employee DTO for create/update operations")
public class EmployeeDto {
    @Schema(description = "Employee name", example = "John Doe")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "Unique badge ID", example = "BADGE12345")
    @NotBlank
    @Size(max = 50)
    private String badgeId;

    @Schema(description = "Employee role", example = "WORKER")
    @NotBlank
    @Size(max = 30)
    private String role;

    @Schema(description = "Department name", example = "Shipping")
    @NotBlank
    @Size(max = 50)
    private String department;

    @Schema(description = "Shift group", example = "A")
    @Size(max = 50)
    private String shiftGroup;

    @Schema(description = "Hire date", example = "2023-01-15")
    @NotNull
    private LocalDate hireDate;

    @Schema(description = "Employee status", example = "ACTIVE")
    @NotBlank
    @Size(max = 20)
    private String status;
}
