package com.wms.employee.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API responses.
 */
public class EmployeeDto {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    // Audit fields
    private String createdBy;
    private String updatedBy;
    private String createdAt;
    private String updatedAt;

    // Getters and setters omitted for brevity
}
