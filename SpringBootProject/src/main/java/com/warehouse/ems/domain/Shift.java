package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Shift entity for scheduling and rotations.
 */
@Entity
@Table(name = "shift")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String rotationPattern;
    private String blackoutDates;
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}
