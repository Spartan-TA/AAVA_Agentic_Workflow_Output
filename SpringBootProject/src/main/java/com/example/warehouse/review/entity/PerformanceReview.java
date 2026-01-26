package com.example.warehouse.review.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column(nullable = false)
    private String reviewer;

    @Column(nullable = false)
    private String rating; // e.g., EXCEEDS, MEETS, BELOW

    @Column
    private String comments;

    // Constructors
    public PerformanceReview() {}

    public PerformanceReview(Long employeeId, LocalDate reviewDate, String reviewer, String rating, String comments) {
        this.employeeId = employeeId;
        this.reviewDate = reviewDate;
        this.reviewer = reviewer;
        this.rating = rating;
        this.comments = comments;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
