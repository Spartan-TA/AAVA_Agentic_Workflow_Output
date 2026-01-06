package com.company.wms.attendance.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (clock-in, clock-out, etc.) for an employee.
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

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private String eventType; // e.g., CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END

    @Column
    private String remarks;
}
