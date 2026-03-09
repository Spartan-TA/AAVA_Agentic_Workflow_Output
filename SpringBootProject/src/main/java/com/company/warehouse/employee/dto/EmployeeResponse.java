package com.company.warehouse.employee.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for returning Employee data.
 */
@Data
public class EmployeeResponse {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
