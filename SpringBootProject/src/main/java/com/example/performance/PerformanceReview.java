package com.example.performance;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeId;
    private LocalDate reviewDate;
    private String reviewer;
    private String comments;
    private int rating;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}