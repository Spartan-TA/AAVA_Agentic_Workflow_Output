package com.example.platform.dto;

import javax.validation.constraints.Size;

/**
 * DTO for updating user profile.
 */
public class UpdateProfileRequest {
    @Size(max = 250)
    private String bio;

    @Size(max = 255)
    private String avatarUrl;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
