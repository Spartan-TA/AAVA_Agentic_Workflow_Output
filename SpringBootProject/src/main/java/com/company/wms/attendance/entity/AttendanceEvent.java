package com.company.wms.attendance.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Attendance event entity for clock-in/out tracking.
 */
@Entity
@Table(name = "attendance_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotBlank
    @Column(nullable = false)
    private String type; // CLOCK_IN, CLOCK_OUT

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotBlank
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotNull
    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "hours_worked")
    private Double hoursWorked;
}
