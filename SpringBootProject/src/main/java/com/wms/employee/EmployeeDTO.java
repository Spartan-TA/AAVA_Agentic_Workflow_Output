package com.wms.employee;

import lombok.*;
import java.time.LocalDate;

/**
 * Employee Data Transfer Object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}