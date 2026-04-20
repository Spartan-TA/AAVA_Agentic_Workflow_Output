package com.warehouse.management.employee.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for returning Employee data in API responses.
 */
@Data
@Builder
public class EmployeeResponseDTO {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
