package com.example.platform.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for editing a post.
 */
public class EditPostRequest {
    @NotBlank
    @Size(max = 1000)
    private String content;

    @Size(max = 255)
    private String imageUrl;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
