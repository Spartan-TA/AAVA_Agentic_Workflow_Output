package com.warehouse.employee.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for employee performance review.
 */
public class PerformanceReviewDto {
    private Long reviewId;
    @NotNull
    private Long employeeId;
    @NotNull
    private LocalDate reviewDate;
    @NotNull
    private String reviewerName;
    private String comments;
    private Integer rating;

    public PerformanceReviewDto() {}

    public PerformanceReviewDto(Long reviewId, Long employeeId, LocalDate reviewDate, String reviewerName, String comments, Integer rating) {
        this.reviewId = reviewId;
        this.employeeId = employeeId;
        this.reviewDate = reviewDate;
        this.reviewerName = reviewerName;
        this.comments = comments;
        this.rating = rating;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
