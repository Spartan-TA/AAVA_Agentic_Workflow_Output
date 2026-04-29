package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class OffboardingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String taskName;
    private String description;
    private String status; // e.g., PENDING, COMPLETED, SKIPPED
    private LocalDate dueDate;
    private LocalDate completedDate;

    public OffboardingTask() {}

    // Getters and setters omitted for brevity
}
