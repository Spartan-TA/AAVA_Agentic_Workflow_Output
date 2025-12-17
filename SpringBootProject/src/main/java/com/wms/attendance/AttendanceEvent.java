package com.wms.attendance;

import javax.persistence.*;
import java.time.LocalDateTime;

import com.wms.employee.Employee;

/**
 * Entity representing an attendance event (clock-in/clock-out).
 */
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private String eventType; // CLOCK_IN, CLOCK_OUT

    private String deviceId;
    private String location;
    private Boolean geofenceValid;

    // Getters and setters omitted for brevity
    // ...
}
