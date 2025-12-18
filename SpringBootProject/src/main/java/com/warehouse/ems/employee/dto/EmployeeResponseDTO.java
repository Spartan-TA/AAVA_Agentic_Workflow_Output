package com.warehouse.ems.employee.dto;

import com.warehouse.ems.employee.entity.Employee;

/**
 * DTO for employee response data.
 */
public class EmployeeResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Employee.Role role;
    private boolean active;

    // Getters and setters omitted for brevity

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Employee.Role getRole() { return role; }
    public void setRole(Employee.Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
