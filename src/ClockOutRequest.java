package com.company.wms.attendance;

import javax.validation.constraints.NotNull;

/**
 * DTO for clock-out requests.
 */
public class ClockOutRequest {
    @NotNull
    private Long employeeId;

    @NotNull
    private String deviceId;

    @NotNull
    private GeoLocation geoLocation;

    public ClockOutRequest() {}

    public ClockOutRequest(Long employeeId, String deviceId, GeoLocation geoLocation) {
        this.employeeId = employeeId;
        this.deviceId = deviceId;
        this.geoLocation = geoLocation;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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
}
