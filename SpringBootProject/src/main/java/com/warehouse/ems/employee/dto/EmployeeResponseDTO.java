package com.warehouse.ems.employee.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO for employee API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
