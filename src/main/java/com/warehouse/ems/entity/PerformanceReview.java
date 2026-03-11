package com.warehouse.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "review_period", nullable = false)
    private String reviewPeriod; // Q1, Q2, Annual, etc.

    @Column(name = "goals")
    private String goals;

    @Column(name = "competencies")
    private String competencies;

    @Column(name = "ratings")
    private String ratings;

    @Column(name = "comments")
    private String comments;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "status")
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED

    // Getters and setters
    // ... (omitted for brevity)
}
