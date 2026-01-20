package com.wms.attendance.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for AttendanceEvent entity.
 * Used for REST API requests and responses.
 */
public class AttendanceEventDto implements Serializable {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Event type is required")
    @Size(max = 50, message = "Event type must be less than 50 characters")
    private String eventType;

    @NotNull(message = "Event timestamp is required")
    private LocalDateTime eventTimestamp;

    // Constructors
    public AttendanceEventDto() {}

    public AttendanceEventDto(Long id, Long employeeId, String eventType, LocalDateTime eventTimestamp) {
        this.id = id;
        this.employeeId = employeeId;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }

    // Optionally, add mapping from Entity
    public AttendanceEventDto(com.wms.attendance.domain.AttendanceEvent event) {
        this.id = event.getId();
        this.employeeId = event.getEmployeeId();
        this.eventType = event.getEventType();
        this.eventTimestamp = event.getEventTimestamp();
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
