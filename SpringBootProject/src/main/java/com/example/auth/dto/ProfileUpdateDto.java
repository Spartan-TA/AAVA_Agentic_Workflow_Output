package com.example.auth.dto;

import lombok.Data;

/**
 * DTO for profile update requests.
 */
@Data
public class ProfileUpdateDto {
    private String fullName;
    private String profileImageUrl;
    private String email; // For email change and verification
}
