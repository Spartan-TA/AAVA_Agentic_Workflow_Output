package com.company.wms.attendance;

import java.time.LocalDateTime;

/**
 * DTO for attendance event responses.
 */
public class AttendanceDTO {
    private Long id;
    private Long employeeId;
    private AttendanceType type;
    private LocalDateTime timestamp;
    private String deviceId;
    private GeoLocation geoLocation;
    private boolean correctionRequested;

    public AttendanceDTO() {}

    public AttendanceDTO(Long id, Long employeeId, AttendanceType type, LocalDateTime timestamp, String deviceId, GeoLocation geoLocation, boolean correctionRequested) {
        this.id = id;
        this.employeeId = employeeId;
        this.type = type;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.geoLocation = geoLocation;
        this.correctionRequested = correctionRequested;
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
