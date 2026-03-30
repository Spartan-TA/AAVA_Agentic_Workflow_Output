package com.companyname.wems.employee.dto;

import com.companyname.wems.employee.entity.Employee;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee creation and update requests
 * 
 * This DTO is used for API requests to create or update employee records.
 * It includes validation annotations to ensure data integrity before
 * processing in the service layer.
 * 
 * Validation rules:
 * - Name: Required, 1-100 characters
 * - Badge ID: Required, alphanumeric, 5-20 characters
 * - Role: Required, must be valid enum value
 * - Department: Required
 * - Shift Group: Required
 * - Hire Date: Required, cannot be in the future
 * - Status: Required, must be valid enum value
 * 
 * @author WEMS Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {
    
    /**
     * Employee full name
     */
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    /**
     * Unique badge identifier
     */
    @NotBlank(message = "Badge ID is required")
    @Pattern(regexp = "^[A-Z0-9]{5,20}$", 
             message = "Badge ID must be 5-20 alphanumeric uppercase characters")
    private String badgeId;

    /**
     * Employee role for RBAC
     */
    @NotNull(message = "Role is required")
    private Employee.Role role;

    /**
     * Department assignment
     */
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String department;

    /**
     * Shift group for scheduling
     */
    @NotBlank(message = "Shift group is required")
    @Size(max = 50, message = "Shift group cannot exceed 50 characters")
    private String shiftGroup;

    /**
     * Date employee was hired
     */
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    /**
     * Current employment status
     */
    @NotNull(message = "Status is required")
    private Employee.Status status;

    /**
     * Convert DTO to Employee entity
     * 
     * @return Employee entity populated with DTO values
     */
    public Employee toEntity() {
        return Employee.builder()
                .name(this.name)
                .badgeId(this.badgeId)
                .role(this.role)
                .department(this.department)
                .shiftGroup(this.shiftGroup)
                .hireDate(this.hireDate)
                .status(this.status)
                .build();
    }
}