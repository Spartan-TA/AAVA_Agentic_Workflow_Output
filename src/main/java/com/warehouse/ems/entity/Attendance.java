package com.warehouse.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "geofence_location")
    private String geofenceLocation;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "status")
    private String status; // e.g., NORMAL, MISSED, CORRECTED

    // Getters and setters
    // ... (omitted for brevity)
}
