package com.wms.ems.employee.dto;

import java.time.LocalDate;

/**
 * DTO for creating or updating Employee records.
 */
public class EmployeeRequestDTO {
    public String name;
    public String badgeId;
    public Long roleId;
    public Long departmentId;
    public Long shiftGroupId;
    public LocalDate hireDate;
    public String status;
}
