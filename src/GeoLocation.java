package com.company.wms.attendance;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * Embeddable class representing a geographical location.
 */
@Embeddable
public class GeoLocation {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    public GeoLocation() {}

    public GeoLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
