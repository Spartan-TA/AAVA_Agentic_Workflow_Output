package com.company.wem.performance.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Map;
import com.company.wem.employee.entity.Employee;
import com.company.wem.performance.enums.ReviewStatus;

/**
 * Entity for employee performance reviews.
 */
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @ElementCollection
    @CollectionTable(name = "performance_ratings", joinColumns = @JoinColumn(name = "review_id"))
    @MapKeyColumn(name = "competency")
    @Column(name = "rating")
    private Map<String, Integer> ratings;

    @Column(length = 2000)
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Employee getReviewer() { return reviewer; }
    public void setReviewer(Employee reviewer) { this.reviewer = reviewer; }
    public Map<String, Integer> getRatings() { return ratings; }
    public void setRatings(Map<String, Integer> ratings) { this.ratings = ratings; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
}