package com.wms.attendance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event for an employee.
 */
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Event type is required")
    private String eventType; // e.g., CLOCK_IN, CLOCK_OUT

    @NotNull(message = "Event timestamp is required")
    private LocalDateTime eventTimestamp;

    // Constructors
    public AttendanceEvent() {}

    public AttendanceEvent(Long employeeId, String eventType, LocalDateTime eventTimestamp) {
        this.employeeId = employeeId;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}
