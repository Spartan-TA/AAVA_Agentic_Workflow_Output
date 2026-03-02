package com.wms.employee;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee entity.
 * Used for API request/response contracts to decouple API layer from domain model.
 * 
 * Validation Rules:
 * - Badge ID: Required, 3-32 characters
 * - Name: Required, 2-128 characters
 * - Role: Required
 * - Department: Required
 * - Hire Date: Required, cannot be in the future
 * - Status: Required
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    
    /**
     * Employee ID (null for creation requests).
     */
    private Long id;
    
    /**
     * Unique badge identifier.
     */
    @NotBlank(message = "Badge ID is required")
    @Size(min = 3, max = 32, message = "Badge ID must be between 3 and 32 characters")
    private String badgeId;
    
    /**
     * Employee full name.
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 128, message = "Name must be between 2 and 128 characters")
    private String name;
    
    /**
     * Job role (e.g., WORKER, SUPERVISOR, MANAGER).
     */
    @NotBlank(message = "Role is required")
    private String role;
    
    /**
     * Department assignment (e.g., Receiving, Shipping, Inventory).
     */
    @NotBlank(message = "Department is required")
    private String department;
    
    /**
     * Shift group for rotating schedules (e.g., A, B, C).
     */
    private String shiftGroup;
    
    /**
     * Date of hire.
     */
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    /**
     * Current employment status.
     */
    @NotNull(message = "Status is required")
    private EmployeeStatus status;
}
