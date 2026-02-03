package com.company.wms.attendance;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for clock-in requests.
 */
public class ClockInRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    @Size(max = 64)
    private String deviceId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    public ClockInRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
