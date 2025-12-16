package com.warehouse.employee.dto;

import java.time.LocalDate;

/**
 * DTO for Employee API responses.
 */
public class EmployeeResponseDTO {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    // Getters and setters omitted for brevity
}
