package com.warehouse.ems.dto;

import java.time.LocalDate;

public class EmployeeCreateDTO {
    private String badgeId;
    private String name;
    private Long roleId;
    private String department;
    private LocalDate hireDate;

    // Getters and Setters
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
