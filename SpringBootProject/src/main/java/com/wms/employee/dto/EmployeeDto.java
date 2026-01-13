package com.wms.employee.dto;

import com.wms.security.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
