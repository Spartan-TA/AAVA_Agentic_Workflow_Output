package com.warehousemgmt.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for Employee API responses.
 */
@Data
public class EmployeeResponseDTO {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private Boolean deleted;
    private String tenantId;
}
