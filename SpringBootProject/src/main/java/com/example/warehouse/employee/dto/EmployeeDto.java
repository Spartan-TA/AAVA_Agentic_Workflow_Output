package com.example.warehouse.employee.dto;

import java.time.LocalDate;

public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private LocalDate hireDate;
    private boolean active;

    // Constructors
    public EmployeeDto() {}

    public EmployeeDto(Long id, String firstName, String lastName, String email, String position, LocalDate hireDate, boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
        this.active = active;
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
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
