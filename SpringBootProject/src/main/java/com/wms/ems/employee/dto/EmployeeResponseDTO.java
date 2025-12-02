package com.wms.ems.employee.dto;

import java.time.LocalDate;

/**
 * DTO for returning Employee data in API responses.
 */
public class EmployeeResponseDTO {
    public Long id;
    public String name;
    public String badgeId;
    public String roleName;
    public String departmentName;
    public String shiftGroupName;
    public LocalDate hireDate;
    public String status;
    public Boolean deleted;
}
