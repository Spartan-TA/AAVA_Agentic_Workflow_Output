package com.warehouse.ems.attendance;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Attendance event for clock-in/out.
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

    @NotNull
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
}
