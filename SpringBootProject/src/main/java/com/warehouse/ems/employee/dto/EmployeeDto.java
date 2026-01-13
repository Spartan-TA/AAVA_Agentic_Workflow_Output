package com.warehouse.ems.employee.dto;

import com.warehouse.ems.rbac.Role;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    private String badgeId;
    private String name;
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private Long warehouseId;
}