package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (clock in/out).
 */
@Entity
@Table(name = "attendance_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String location;

    public enum AttendanceType {
        CLOCK_IN, CLOCK_OUT
    }
}
