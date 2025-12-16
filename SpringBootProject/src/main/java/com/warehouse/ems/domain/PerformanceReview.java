package com.warehouse.ems.domain;

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

    private String cycle;
    private String goals;
    private String competencies;
    private Integer rating;
    private String comments;
    private Boolean acknowledged = false;
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}
