package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public class EmployeeDto {
    @NotBlank
    private String name;

    @NotBlank
    private String badgeId;

    @NotNull
    private Long departmentId;

    @NotNull
    @Size(min = 1)
    private List<Long> roleIds;

    @NotBlank
    private String status;

    @NotNull
    private LocalDate hireDate;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
