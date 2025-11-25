package com.warehouse.ems.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event for an employee.
 */
@Entity
@Table(name = "attendance_event")
@Data
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @NotNull
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
