package com.wms.ems.employee.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
