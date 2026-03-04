package com.wms.employee.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests and responses.
 */
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;

    // Getters and setters omitted for brevity
    // ...
}
