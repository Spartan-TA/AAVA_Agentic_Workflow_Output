package com.companyname.wems.employee.dto;

import com.companyname.wems.employee.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Employee API responses
 * 
 * This DTO is used to return employee data in API responses.
 * It includes all employee fields plus audit information.
 * Separates the API contract from the internal entity structure.
 * 
 * @author WEMS Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    
    /**
     * Employee ID
     */
    private Long id;
    
    /**
     * Employee full name
     */
    private String name;
    
    /**
     * Unique badge identifier
     */
    private String badgeId;
    
    /**
     * Employee role
     */
    private Employee.Role role;
    
    /**
     * Department assignment
     */
    private String department;
    
    /**
     * Shift group
     */
    private String shiftGroup;
    
    /**
     * Hire date
     */
    private LocalDate hireDate;
    
    /**
     * Current status
     */
    private Employee.Status status;
    
    /**
     * Record creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
    
    /**
     * User who created the record
     */
    private String createdBy;
    
    /**
     * User who last updated the record
     */
    private String updatedBy;

    /**
     * Convert Employee entity to response DTO
     * 
     * @param employee Employee entity
     * @return EmployeeResponse DTO
     */
    public static EmployeeResponse fromEntity(Employee employee) {
        if (employee == null) {
            return null;
        }
        
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }
}