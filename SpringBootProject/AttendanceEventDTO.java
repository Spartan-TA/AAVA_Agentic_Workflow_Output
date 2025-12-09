package com.example.warehousemanagement.dto;

import com.example.warehousemanagement.entity.AttendanceEvent;
import java.time.LocalDate;

/**
 * Data Transfer Object for AttendanceEvent entity.
 */
public class AttendanceEventDTO {
    private Long id;
    private Long employeeId;
    private String eventType;
    private LocalDate eventDate;
    private String notes;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static AttendanceEventDTO fromEntity(AttendanceEvent event) {
        AttendanceEventDTO dto = new AttendanceEventDTO();
        dto.setId(event.getId());
        dto.setEmployeeId(event.getEmployee().getId());
        dto.setEventType(event.getEventType());
        dto.setEventDate(event.getEventDate());
        dto.setNotes(event.getNotes());
        return dto;
    }

    public AttendanceEvent toEntity() {
        AttendanceEvent event = new AttendanceEvent();
        event.setId(this.id);
        // Employee must be set in service layer
        event.setEventType(this.eventType);
        event.setEventDate(this.eventDate);
        event.setNotes(this.notes);
        return event;
    }
}
