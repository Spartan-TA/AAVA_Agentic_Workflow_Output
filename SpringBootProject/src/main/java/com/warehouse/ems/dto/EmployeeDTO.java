package com.warehouse.ems.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee entity.
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
