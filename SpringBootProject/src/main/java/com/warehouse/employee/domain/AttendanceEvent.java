package com.warehouse.employee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AttendanceEvent entity for clock-in/out and attendance tracking.
 */
@Entity
@Table(name = "attendance_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "event_type", nullable = false, length = 16)
    private String eventType; // CLOCK_IN, CLOCK_OUT

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "geofence_valid")
    private Boolean geofenceValid = true;

    @Column(name = "device_info", length = 128)
    private String deviceInfo;

    @Column(name = "hours_worked")
    private Double hoursWorked;

    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
