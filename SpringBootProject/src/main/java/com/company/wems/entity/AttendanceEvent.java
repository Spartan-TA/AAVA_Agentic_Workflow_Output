package com.company.wems.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Attendance event entity for clock in/out.
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

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "event_type", nullable = false)
    private String eventType; // CLOCK_IN, CLOCK_OUT

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "geofence_valid")
    private Boolean geofenceValid = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
