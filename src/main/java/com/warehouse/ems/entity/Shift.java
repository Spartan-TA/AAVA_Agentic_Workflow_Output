package com.warehouse.ems.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "shifts")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "recurrence_pattern")
    private String recurrencePattern;

    @Column(name = "overtime_rule")
    private String overtimeRule;

    @ManyToMany(mappedBy = "shifts")
    private Set<Employee> employees;

    // Getters and setters
    // ... (omitted for brevity)
}
