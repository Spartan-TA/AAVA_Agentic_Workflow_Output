package com.warehouse.employee.management.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for employee API responses.
 */
@Data
public class EmployeeResponseDto {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
