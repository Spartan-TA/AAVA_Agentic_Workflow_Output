package com.example.warehouse.attendance.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private String eventType; // IN, OUT, BREAK, etc.

    // Constructors
    public AttendanceEvent() {}

    public AttendanceEvent(Long employeeId, LocalDateTime eventTime, String eventType) {
        this.employeeId = employeeId;
        this.eventTime = eventTime;
        this.eventType = eventType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}
