package com.warehouse.ems.attendance.dto;

import com.warehouse.ems.attendance.entity.AttendanceEvent;
import java.time.LocalDateTime;

/**
 * DTO for attendance response data.
 */
public class AttendanceResponseDTO {
    private Long id;
    private Long employeeId;
    private AttendanceEvent.EventType eventType;
    private LocalDateTime eventTime;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public AttendanceEvent.EventType getEventType() { return eventType; }
    public void setEventType(AttendanceEvent.EventType eventType) { this.eventType = eventType; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
