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
    private Double latitude;
    private Double longitude;
    private Boolean correctionRequested;

    public AttendanceDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public AttendanceType getType() { return type; }
    public void setType(AttendanceType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getCorrectionRequested() { return correctionRequested; }
    public void setCorrectionRequested(Boolean correctionRequested) { this.correctionRequested = correctionRequested; }
}
