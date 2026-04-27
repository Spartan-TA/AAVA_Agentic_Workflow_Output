package com.warehouse.ems.employee.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class EmployeeRequestDto {
    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    private String name;

    @NotBlank
    private String role;

    @NotBlank
    private String department;

    @NotBlank
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    private String status;

    // Getters and setters
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getShiftGroup() { return shiftGroup; }
    public void setShiftGroup(String shiftGroup) { this.shiftGroup = shiftGroup; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
