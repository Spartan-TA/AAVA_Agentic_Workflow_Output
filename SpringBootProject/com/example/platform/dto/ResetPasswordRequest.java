package com.example.platform.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for password reset requests.
 */
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, max = 100)
    private String newPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
