package com.warehouse.ems.dto;

public class LeaveApprovalDTO {
    private boolean approved;
    private String comments;

    // Getters and Setters
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
