package com.companyname.wems.attendance.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * AttendanceEvent entity for Time & Attendance (E04)
 * Supports geolocation and device info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String eventType; // CLOCK_IN, CLOCK_OUT

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String location; // Geolocation (lat,long)

    @Column
    private String deviceInfo;
}
