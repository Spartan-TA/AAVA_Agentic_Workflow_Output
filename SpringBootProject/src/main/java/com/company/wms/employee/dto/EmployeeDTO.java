package com.company.wms.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee entity.
 * Used for API requests and responses.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee data transfer object")
public class EmployeeDTO {
    
    @Schema(description = "Employee ID", example = "1")
    private Long id;

    @NotBlank(message = "Badge ID is required")
    @Size(max = 32, message = "Badge ID must not exceed 32 characters")
    @Schema(description = "Unique badge identifier", example = "EMP001", required = true)
    private String badgeId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Employee full name", example = "John Doe", required = true)
    private String name;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|HR|SUPERVISOR|WORKER", message = "Role must be ADMIN, HR, SUPERVISOR, or WORKER")
    @Schema(description = "Employee role", example = "WORKER", required = true, allowableValues = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    private String role;

    @Size(max = 50, message = "Department must not exceed 50 characters")
    @Schema(description = "Department assignment", example = "Shipping")
    private String department;

    @Size(max = 50, message = "Shift group must not exceed 50 characters")
    @Schema(description = "Shift group for scheduling", example = "DAY_SHIFT")
    private String shiftGroup;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Hire date", example = "2024-01-15")
    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACTIVE|INACTIVE|ON_LEAVE|TERMINATED", message = "Status must be ACTIVE, INACTIVE, ON_LEAVE, or TERMINATED")
    @Schema(description = "Employment status", example = "ACTIVE", required = true, allowableValues = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"})
    private String status;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Email address", example = "john.doe@company.com")
    private String email;

    @Pattern(regexp = "^\+?[1-9]\d{1,14}$", message = "Phone must be a valid international format")
    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "Tenant ID for multi-tenancy", example = "WAREHOUSE_A")
    private String tenantId;
}