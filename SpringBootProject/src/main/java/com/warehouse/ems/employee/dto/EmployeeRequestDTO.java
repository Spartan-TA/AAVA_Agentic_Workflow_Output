package com.warehouse.ems.employee.dto;

import com.warehouse.ems.employee.entity.Employee;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for employee creation and update requests.
 */
public class EmployeeRequestDTO {
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    private Employee.Role role;

    // Getters and setters omitted for brevity

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Employee.Role getRole() { return role; }
    public void setRole(Employee.Role role) { this.role = role; }
}
