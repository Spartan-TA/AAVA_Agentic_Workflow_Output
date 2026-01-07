package com.example.usermanagement.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for user registration requests.
 */
public class RegisterRequest {
    /**
     * User's email address.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User's password.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * User's first name.
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * User's last name.
     */
    @NotBlank(message = "Last name is required")
    private String lastName;

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
