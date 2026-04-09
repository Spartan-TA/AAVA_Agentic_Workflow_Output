package com.warehouse.ems.employee.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO for updating an Employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateDto {
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private String status;
    private LocalDate hireDate;
}
