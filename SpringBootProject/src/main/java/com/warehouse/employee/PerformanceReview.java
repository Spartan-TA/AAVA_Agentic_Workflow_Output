package com.warehouse.employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PerformanceReview entity for employee reviews and goals.
 */
@Entity
@Table(name = "performance_review")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "review_period", nullable = false)
    private String reviewPeriod;

    @Column(name = "reviewer")
    private String reviewer;

    @Column(name = "goals")
    private String goals;

    @Column(name = "competencies")
    private String competencies;

    @Column(name = "ratings")
    private String ratings;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "acknowledged_by_employee")
    private Boolean acknowledgedByEmployee = false;

    @Column(name = "acknowledged_by_supervisor")
    private Boolean acknowledgedBySupervisor = false;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and setters omitted for brevity
}
