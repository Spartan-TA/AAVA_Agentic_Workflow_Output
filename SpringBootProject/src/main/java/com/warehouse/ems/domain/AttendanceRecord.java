package com.warehouse.ems.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an employee's attendance record.
 */
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotNull
    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    private ShiftTemplate shift;

    @Column(name = "correction_requested", nullable = false)
    private boolean correctionRequested = false;

    // Getters and setters omitted for brevity
}
