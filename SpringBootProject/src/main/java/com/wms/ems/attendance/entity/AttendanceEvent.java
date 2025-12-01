package com.wms.ems.attendance.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import com.wms.ems.employee.entity.Employee;

/**
 * AttendanceEvent entity representing clock-in, clock-out, and other attendance events for employees.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType; // e.g., CLOCK_IN, CLOCK_OUT, BREAK

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "location", length = 128)
    private String location;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}