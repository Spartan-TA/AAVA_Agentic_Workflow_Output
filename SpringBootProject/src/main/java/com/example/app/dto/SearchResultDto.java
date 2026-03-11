package com.example.app.dto;

import com.example.app.entity.User;
import lombok.Data;

@Data
public class SearchResultDto {
    private String username;
    private String email;
    private String profilePictureUrl;

    public static SearchResultDto fromEntity(User user) {
        SearchResultDto dto = new SearchResultDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }
}
