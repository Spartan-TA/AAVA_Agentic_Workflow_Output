package com.company.warehouse.employee.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO for Employee API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private String tenantId;
}
