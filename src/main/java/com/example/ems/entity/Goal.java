package com.example.ems.entity;

import jakarta.persistence.*;

@Entity
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id")
    private PerformanceReview performanceReview;

    private String title;
    private String description;
    private Integer rating;
    private String status; // e.g., NOT_STARTED, IN_PROGRESS, COMPLETED

    public Goal() {}

    // Getters and setters omitted for brevity
}
