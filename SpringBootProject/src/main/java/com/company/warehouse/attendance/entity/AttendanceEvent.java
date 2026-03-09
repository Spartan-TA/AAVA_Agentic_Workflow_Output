package com.company.warehouse.attendance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (clock-in/clock-out) for an employee.
 */
@Entity
@Table(name = "attendance_events", indexes = {
        @Index(name = "idx_attendance_employee", columnList = "employeeId, eventTime")
})
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EventType eventType;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 255)
    private String locationDescription;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum EventType {
        CLOCK_IN, CLOCK_OUT
    }

    // Getters and setters omitted for brevity
    // ...
}
