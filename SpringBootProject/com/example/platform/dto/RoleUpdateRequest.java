package com.example.platform.dto;

import javax.validation.constraints.NotBlank;

/**
 * DTO for updating user role.
 */
public class RoleUpdateRequest {
    @NotBlank
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
