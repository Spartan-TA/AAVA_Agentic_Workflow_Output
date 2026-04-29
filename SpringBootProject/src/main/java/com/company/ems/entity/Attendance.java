package com.company.ems.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
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
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "geofence_location")
    private String geofenceLocation;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;

    // Getters and setters omitted for brevity
}
