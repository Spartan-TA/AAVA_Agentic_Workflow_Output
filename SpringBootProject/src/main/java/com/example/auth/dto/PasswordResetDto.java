package com.example.auth.dto;

import lombok.Data;

/**
 * DTO for password reset requests.
 */
@Data
public class PasswordResetDto {
    private String email;
    private String token;
    private String newPassword;
}
