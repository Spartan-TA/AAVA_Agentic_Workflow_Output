package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String cycle; // e.g., Q1 2024, Annual 2023
    private LocalDate reviewDate;
    private String reviewer;
    private String status; // e.g., DRAFT, SUBMITTED, SIGNED_OFF

    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Goal> goals;

    @Column(length = 2000)
    private String comments;

    private Integer overallRating;

    public PerformanceReview() {}

    // Getters and setters omitted for brevity
}
