package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Attendance event entity for clock-in/clock-out tracking.
 */
@Entity
@Table(name = "attendance_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    private String deviceId;
    private String geofenceId;
    @Builder.Default
    private boolean correction = false;

    public enum EventType {
        CLOCK_IN, CLOCK_OUT
    }
}
