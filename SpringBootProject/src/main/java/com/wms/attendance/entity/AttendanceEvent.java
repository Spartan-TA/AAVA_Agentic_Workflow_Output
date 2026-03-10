package com.wms.attendance.entity;

import javax.persistence.*;
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

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column
    private String location;

    public enum EventType {
        CLOCK_IN, CLOCK_OUT
    }

    public AttendanceEvent() {}

    public AttendanceEvent(Long employeeId, LocalDateTime eventTime, EventType eventType, String location) {
        this.employeeId = employeeId;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
