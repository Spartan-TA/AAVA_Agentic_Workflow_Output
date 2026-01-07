package com.example.auth.dto;

import lombok.Data;

/**
 * DTO for user registration requests.
 */
@Data
public class RegisterRequestDto {
    private String email;
    private String password;
    private String fullName;
}
