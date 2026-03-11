package com.example.platform.dto;

import java.util.Map;

/**
 * DTO for analytics data.
 */
public class AnalyticsDto {
    private int totalUsers;
    private int totalPosts;
    private int totalComments;
    private int totalLikes;
    private Map<String, Integer> postsPerDay;
    private Map<String, Integer> registrationsPerDay;

    public AnalyticsDto() {}

    public AnalyticsDto(int totalUsers, int totalPosts, int totalComments, int totalLikes, Map<String, Integer> postsPerDay, Map<String, Integer> registrationsPerDay) {
        this.totalUsers = totalUsers;
        this.totalPosts = totalPosts;
        this.totalComments = totalComments;
        this.totalLikes = totalLikes;
        this.postsPerDay = postsPerDay;
        this.registrationsPerDay = registrationsPerDay;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Map<String, Integer> getPostsPerDay() {
        return postsPerDay;
    }

    public void setPostsPerDay(Map<String, Integer> postsPerDay) {
        this.postsPerDay = postsPerDay;
    }

    public Map<String, Integer> getRegistrationsPerDay() {
        return registrationsPerDay;
    }

    public void setRegistrationsPerDay(Map<String, Integer> registrationsPerDay) {
        this.registrationsPerDay = registrationsPerDay;
    }
}
