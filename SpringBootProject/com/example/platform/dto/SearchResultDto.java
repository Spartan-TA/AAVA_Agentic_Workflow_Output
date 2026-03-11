package com.example.platform.dto;

import java.util.List;

/**
 * DTO for search results.
 */
public class SearchResultDto {
    private List<UserProfileDto> users;
    private List<PostDto> posts;

    public SearchResultDto() {}

    public SearchResultDto(List<UserProfileDto> users, List<PostDto> posts) {
        this.users = users;
        this.posts = posts;
    }

    public List<UserProfileDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfileDto> users) {
        this.users = users;
    }

    public List<PostDto> getPosts() {
        return posts;
    }

    public void setPosts(List<PostDto> posts) {
        this.posts = posts;
    }
}
