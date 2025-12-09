package com.example.warehousemanagement.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * Entity representing a Performance Review for an employee.
 */
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    public enum Status {
        DRAFT, SUBMITTED, ACKNOWLEDGED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "review_period", nullable = false)
    private String reviewPeriod;

    @ElementCollection
    @CollectionTable(name = "performance_ratings", joinColumns = @JoinColumn(name = "review_id"))
    @MapKeyColumn(name = "criteria")
    @Column(name = "rating")
    private Map<String, Integer> ratings;

    @Column(length = 2000)
    private String comments;

    @Column(length = 2000)
    private String goals;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getReviewPeriod() { return reviewPeriod; }
    public void setReviewPeriod(String reviewPeriod) { this.reviewPeriod = reviewPeriod; }

    public Map<String, Integer> getRatings() { return ratings; }
    public void setRatings(Map<String, Integer> ratings) { this.ratings = ratings; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Employee getReviewer() { return reviewer; }
    public void setReviewer(Employee reviewer) { this.reviewer = reviewer; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
}
