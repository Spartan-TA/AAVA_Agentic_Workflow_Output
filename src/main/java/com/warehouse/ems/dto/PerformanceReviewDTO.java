package com.warehouse.ems.dto;

public class PerformanceReviewDTO {
    private Long id;
    private Long employeeId;
    private Long reviewerId;
    private String cycle;
    private String goals;
    private String ratings;
    private String comments;
    private String status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }
    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }
    public String getRatings() { return ratings; }
    public void setRatings(String ratings) { this.ratings = ratings; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
