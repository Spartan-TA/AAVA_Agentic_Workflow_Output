package com.warehouse.attendance;

import com.warehouse.employee.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AttendanceEvent entity for clock-in/out and corrections.
 */
@Entity
@Table(name = "attendance_event")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "event_type", nullable = false)
    private String eventType; // CLOCK_IN, CLOCK_OUT, etc.

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "location")
    private String location;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and setters omitted for brevity
}
