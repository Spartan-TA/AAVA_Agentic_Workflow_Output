package com.warehouse.ems.employee.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object for Employee entity.
 * Used for returning employee data to clients.
 */
public class EmployeeDto {

    /** Employee unique identifier */
    private Long id;

    /** Employee first name */
    @NotNull
    @Size(min = 2, max = 50)
    private String firstName;

    /** Employee last name */
    @NotNull
    @Size(min = 2, max = 50)
    private String lastName;

    /** Employee email address */
    @NotNull
    @Email
    private String email;

    /** Employee phone number */
    @Size(min = 10, max = 20)
    private String phone;

    /** Employee department */
    @NotNull
    @Size(min = 2, max = 50)
    private String department;

    /** Employee role */
    @NotNull
    @Size(min = 2, max = 50)
    private String role;

    /** Employee active status */
    private boolean active;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
