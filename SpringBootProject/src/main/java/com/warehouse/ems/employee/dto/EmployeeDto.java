package com.warehouse.ems.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Employee entity.
 * Used for API requests and responses to decouple the domain model from the API layer.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee data transfer object")
public class EmployeeDto {
    
    @Schema(description = "Unique identifier of the employee", example = "1")
    private Long id;
    
    @NotBlank(message = "Employee name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Full name of the employee", example = "John Doe", required = true)
    private String name;
    
    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    @Schema(description = "Unique badge ID for the employee", example = "EMP001", required = true)
    private String badgeId;
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|HR|SUPERVISOR|WORKER", message = "Role must be one of: ADMIN, HR, SUPERVISOR, WORKER")
    @Schema(description = "Role of the employee", example = "WORKER", required = true, allowableValues = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    private String role;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Schema(description = "Department where the employee works", example = "Warehouse Operations")
    private String department;
    
    @Size(max = 100, message = "Shift group must not exceed 100 characters")
    @Schema(description = "Shift group assignment", example = "Morning Shift")
    private String shiftGroup;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date must be in the past or present")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date when the employee was hired", example = "2024-01-15", required = true)
    private LocalDate hireDate;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACTIVE|INACTIVE|TERMINATED", message = "Status must be one of: ACTIVE, INACTIVE, TERMINATED")
    @Schema(description = "Current employment status", example = "ACTIVE", required = true, allowableValues = {"ACTIVE", "INACTIVE", "TERMINATED"})
    private String status;
    
    @Schema(description = "Timestamp when the employee record was created", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the employee record was last updated", example = "2024-01-20T14:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}