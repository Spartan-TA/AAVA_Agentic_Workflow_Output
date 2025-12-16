package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Attendance entity for clock-in/out events.
 */
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceInfo;
    private String geofenceLocation;
    private Boolean approved = false;
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}
