package com.example.app.dto;

import com.example.app.entity.User;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserProfileDto {
    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    private String profilePictureUrl;

    public static UserProfileDto fromEntity(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }
}
