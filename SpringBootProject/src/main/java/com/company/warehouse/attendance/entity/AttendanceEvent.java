package com.company.warehouse.attendance.entity;

import com.company.warehouse.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity for attendance clock-in/out events.
 */
@Entity
@Table(name = "attendance_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "type", nullable = false)
    private String type; // CLOCK_IN, CLOCK_OUT

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "location")
    private String location;

    @Column(name = "status", nullable = false)
    private String status; // VALID, CORRECTION, APPROVED
}
