package com.wms.review;

import javax.persistence.*;
import java.time.LocalDate;

import com.wms.employee.Employee;

/**
 * Entity representing a performance review for an employee.
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
    private String reviewPeriod; // e.g., "Q1 2024", "2024 Annual"

    @Column(nullable = false)
    private LocalDate reviewDate;

    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private Boolean acknowledgedByEmployee;
    private Boolean acknowledgedBySupervisor;

    // Getters and setters omitted for brevity
    // ...
}
