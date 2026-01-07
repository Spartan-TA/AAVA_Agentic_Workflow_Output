package com.example.usermanagement.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * DTO for user login requests.
 */
public class LoginRequest {
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
    private String password;

    /**
     * CAPTCHA response (if required).
     */
    private String captchaResponse;

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCaptchaResponse() { return captchaResponse; }
    public void setCaptchaResponse(String captchaResponse) { this.captchaResponse = captchaResponse; }
}
