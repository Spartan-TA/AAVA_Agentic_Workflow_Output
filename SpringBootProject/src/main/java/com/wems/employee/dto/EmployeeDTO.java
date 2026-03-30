package com.wems.employee.dto;

import com.wems.employee.domain.Employee;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object for Employee API boundaries.
 * Includes validation and conversion to domain entity.
 */
public class EmployeeDTO {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 20)
    private String badgeId;

    @NotBlank
    @Size(max = 50)
    private String role;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /**
     * Converts DTO to Employee entity.
     * @return Employee
     */
    public Employee toEntity() {
        Employee employee = new Employee();
        employee.setName(this.name);
        employee.setEmail(this.email);
        employee.setBadgeId(this.badgeId);
        employee.setRole(this.role);
        employee.setActive(true);
        return employee;
    }
}
