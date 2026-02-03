package com.company.wms.attendance;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event.
 */
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AttendanceType type;

    @NotNull
    private LocalDateTime timestamp;

    @NotNull
    private String deviceId;

    @Embedded
    private GeoLocation geoLocation;

    private boolean correctionRequested = false;

    public AttendanceEvent() {}

    public AttendanceEvent(Long employeeId, AttendanceType type, LocalDateTime timestamp, String deviceId, GeoLocation geoLocation) {
        this.employeeId = employeeId;
        this.type = type;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.geoLocation = geoLocation;
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

    public AttendanceType getType() {
        return type;
    }

    public void setType(AttendanceType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public boolean isCorrectionRequested() {
        return correctionRequested;
    }

    public void setCorrectionRequested(boolean correctionRequested) {
        this.correctionRequested = correctionRequested;
    }
}
