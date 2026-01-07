package com.example.auth.dto;

import lombok.Data;

/**
 * DTO for login requests.
 */
@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
