package com.warehouse.management.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;

/**
 * DTO for authentication requests.
 */
@Schema(description = "Authentication request DTO")
public class AuthRequest {
    @NotBlank
    @Schema(description = "Username", example = "john.doe")
    private String username;

    @NotBlank
    @Schema(description = "Password", example = "password123")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
