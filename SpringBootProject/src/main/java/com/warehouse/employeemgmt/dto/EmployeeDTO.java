package com.warehouse.employeemgmt.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * EmployeeDTO - Data Transfer Object for Employee entity
 * 
 * Used for API request/response with validation annotations.
 * Separates API layer from domain model.
 * 
 * Features:
 * - Bean validation annotations
 * - Immutable data transfer
 * - Clear API contract
 * - Separation of concerns
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    
    @NotBlank(message = "Badge ID is required")
    @Size(min = 3, max = 50, message = "Badge ID must be between 3 and 50 characters")
    private String badgeId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|HR|SUPERVISOR|WORKER", message = "Role must be ADMIN, HR, SUPERVISOR, or WORKER")
    private String role;

    @NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department must not exceed 50 characters")
    private String department;

    @Size(max = 50, message = "Shift group must not exceed 50 characters")
    private String shiftGroup;

    @PastOrPresent(message = "Hire date must be in the past or present")
    private LocalDate hireDate;
}