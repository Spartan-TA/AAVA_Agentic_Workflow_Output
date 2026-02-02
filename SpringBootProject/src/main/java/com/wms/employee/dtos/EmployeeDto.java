package com.wms.employee.dtos;

import com.wms.common.enums.Role;
import com.wms.common.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String name;
    private String badgeId;
    private Role role;
    private Long departmentId;
    private String departmentName;
    private String shiftGroup;
    private LocalDate hireDate;
    private Status status;
    private boolean deleted;
}
