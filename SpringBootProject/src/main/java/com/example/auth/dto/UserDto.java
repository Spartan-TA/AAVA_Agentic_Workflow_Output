package com.example.auth.dto;

import lombok.*;

/**
 * DTO for user profile and response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private boolean emailVerified;
    private String profileImageUrl;
}
