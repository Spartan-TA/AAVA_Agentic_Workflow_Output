package com.warehouse.ems.employee.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for creating a new Employee.
 * Used in POST /api/employees endpoint.
 */
public class CreateEmployeeRequest {

    /** Employee first name */
    @NotNull(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    /** Employee last name */
    @NotNull(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /** Employee email address */
    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /** Employee phone number */
    @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters")
    private String phone;

    /** Employee department */
    @NotNull(message = "Department is required")
    @Size(min = 2, max = 50, message = "Department must be between 2 and 50 characters")
    private String department;

    /** Employee role */
    @NotNull(message = "Role is required")
    @Size(min = 2, max = 50, message = "Role must be between 2 and 50 characters")
    private String role;

    // Getters and setters
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
}
