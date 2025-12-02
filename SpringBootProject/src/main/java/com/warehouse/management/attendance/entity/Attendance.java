package com.warehouse.management.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Attendance entity for clock-in/out events and corrections.
 */
@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "hours_worked")
    private Double hoursWorked;

    @Column(name = "status")
    private String status;

    @Column(name = "corrections")
    private String corrections;
}
