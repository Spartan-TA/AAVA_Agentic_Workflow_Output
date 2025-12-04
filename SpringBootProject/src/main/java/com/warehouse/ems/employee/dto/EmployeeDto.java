package com.warehouse.ems.employee.dto;

import java.time.LocalDate;

/**
 * DTO for Employee API responses.
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
    // Getters and setters omitted for brevity
}
