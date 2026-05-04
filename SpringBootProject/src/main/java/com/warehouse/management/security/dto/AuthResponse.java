package com.warehouse.management.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for authentication responses (JWT token).
 */
@Schema(description = "Authentication response DTO")
public class AuthResponse {
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    public AuthResponse() {}
    public AuthResponse(String token) { this.token = token; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
