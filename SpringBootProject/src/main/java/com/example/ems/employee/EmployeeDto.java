package com.example.ems.employee;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee.
 */
public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private LocalDate hireDate;

    public EmployeeDto() {}

    public EmployeeDto(Long id, String firstName, String lastName, String email, String role, LocalDate hireDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.hireDate = hireDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
