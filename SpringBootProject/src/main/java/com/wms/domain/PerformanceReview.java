package com.wms.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PerformanceReview extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column(nullable = false)
    private String reviewer;

    @Column(nullable = false)
    private String comments;

    @Column(nullable = false)
    private int score;

    // Getters and setters
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
