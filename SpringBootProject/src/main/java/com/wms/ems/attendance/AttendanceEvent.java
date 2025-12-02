package com.wms.ems.attendance;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type; // IN or OUT

    private String deviceId;
    private String location;
    private Long shiftId;

    public enum EventType {
        IN, OUT
    }

    // Getters and setters omitted for brevity
}
