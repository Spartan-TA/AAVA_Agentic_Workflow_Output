package com.warehouse.management.attendance.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (check-in/check-out).
 */
@Entity
@Table(name = "attendance_events")
@Schema(description = "Attendance event entity for check-in/check-out tracking")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Attendance event ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Employee ID", example = "101")
    private Long employeeId;

    @NotNull
    @Schema(description = "Event type (IN/OUT)", example = "IN")
    private String eventType;

    @NotNull
    @Schema(description = "Event timestamp", example = "2024-05-04T08:00:00")
    private LocalDateTime eventTime;

    @Schema(description = "Latitude of event", example = "37.7749")
    private Double latitude;

    @Schema(description = "Longitude of event", example = "-122.4194")
    private Double longitude;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
