package com.warehouse.management.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee.
 * 
 * Used for API request/response to decouple the entity from external interfaces.
 * Includes validation annotations to ensure data integrity.
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee data transfer object")
public class EmployeeDTO {

    /**
     * Unique badge ID for the employee.
     */
    @Schema(description = "Employee badge ID", example = "EMP12345", required = true)
    @NotBlank(message = "Badge ID is required")
    @Size(max = 32, message = "Badge ID must not exceed 32 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Badge ID must contain only uppercase letters and numbers")
    private String badgeId;

    /**
     * Full name of the employee.
     */
    @Schema(description = "Full name of the employee", example = "John Doe", required = true)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 128, message = "Name must be between 2 and 128 characters")
    private String name;

    /**
     * Role of the employee.
     */
    @Schema(description = "Employee role", example = "WORKER", required = true, 
            allowableValues = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|HR|SUPERVISOR|WORKER)$", 
             message = "Role must be one of: ADMIN, HR, SUPERVISOR, WORKER")
    private String role;

    /**
     * Department where the employee works.
     */
    @Schema(description = "Department", example = "Shipping")
    @Size(max = 64, message = "Department must not exceed 64 characters")
    private String department;

    /**
     * Shift group assignment.
     */
    @Schema(description = "Shift group", example = "A")
    @Size(max = 32, message = "Shift group must not exceed 32 characters")
    private String shiftGroup;

    /**
     * Date when the employee was hired.
     */
    @Schema(description = "Hire date", example = "2023-01-15", required = true)
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    /**
     * Current employment status.
     */
    @Schema(description = "Employment status", example = "ACTIVE", required = true,
            allowableValues = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"})
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|INACTIVE|ON_LEAVE|TERMINATED)$",
             message = "Status must be one of: ACTIVE, INACTIVE, ON_LEAVE, TERMINATED")
    private String status;
}