package com.warehouseems.attendance.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (clock-in/clock-out) for an employee.
 */
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    @PastOrPresent
    private LocalDateTime timestamp;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EventType type; // IN or OUT

    private String deviceId;
    private String location;

    public enum EventType {
        IN, OUT
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}