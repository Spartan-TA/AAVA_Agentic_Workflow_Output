package com.wms.attendance.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (clock in/out).
 */
@Entity
@Table(name = "attendance_event")
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

    @Column(nullable = false)
    private String type; // CLOCK_IN or CLOCK_OUT

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String location;
    private String deviceId;
    private String status; // e.g., VALID, CORRECTED, MISSED
}
