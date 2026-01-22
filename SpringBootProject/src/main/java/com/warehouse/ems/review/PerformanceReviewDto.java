package com.warehouse.ems.review;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for PerformanceReview with validation annotations.
 */
public class PerformanceReviewDto {
    private Long id;

    @NotNull(message = "Employee ID is required.")
    private Long employeeId;

    @NotNull(message = "Reviewer ID is required.")
    private Long reviewerId;

    @NotNull(message = "Period start is required.")
    private LocalDate periodStart;

    @NotNull(message = "Period end is required.")
    private LocalDate periodEnd;

    @NotNull(message = "Overall rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be at most 5.")
    private Integer overallRating;

    @Size(max = 2000, message = "Comments too long.")
    private String comments;

    @NotNull(message = "Status is required.")
    private String status;

    private LocalDateTime acknowledgedDate;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public Integer getOverallRating() { return overallRating; }
    public void setOverallRating(Integer overallRating) { this.overallRating = overallRating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAcknowledgedDate() { return acknowledgedDate; }
    public void setAcknowledgedDate(LocalDateTime acknowledgedDate) { this.acknowledgedDate = acknowledgedDate; }
}
