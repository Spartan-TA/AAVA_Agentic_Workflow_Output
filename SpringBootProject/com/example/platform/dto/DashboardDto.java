package com.example.platform.dto;

/**
 * DTO for dashboard data.
 */
public class DashboardDto {
    private int postCount;
    private int commentCount;
    private int likeCount;
    private int followerCount;
    private int followingCount;
    private int notificationCount;

    public DashboardDto() {}

    public DashboardDto(int postCount, int commentCount, int likeCount, int followerCount, int followingCount, int notificationCount) {
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.notificationCount = notificationCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }
}
