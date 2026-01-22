package com.warehouse.ems.review;

import com.warehouse.ems.employee.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a performance review for an employee.
 */
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required.")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @NotNull(message = "Reviewer is required.")
    private Employee reviewer;

    @NotNull(message = "Period start is required.")
    @Column(nullable = false)
    private LocalDate periodStart;

    @NotNull(message = "Period end is required.")
    @Column(nullable = false)
    private LocalDate periodEnd;

    @NotNull(message = "Overall rating is required.")
    @Column(nullable = false)
    private Integer overallRating;

    @Lob
    private String comments;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required.")
    @Column(nullable = false)
    private Status status;

    private LocalDateTime acknowledgedDate;

    public enum Status {
        DRAFT, SUBMITTED, ACKNOWLEDGED
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Employee getReviewer() { return reviewer; }
    public void setReviewer(Employee reviewer) { this.reviewer = reviewer; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public Integer getOverallRating() { return overallRating; }
    public void setOverallRating(Integer overallRating) { this.overallRating = overallRating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getAcknowledgedDate() { return acknowledgedDate; }
    public void setAcknowledgedDate(LocalDateTime acknowledgedDate) { this.acknowledgedDate = acknowledgedDate; }
}
