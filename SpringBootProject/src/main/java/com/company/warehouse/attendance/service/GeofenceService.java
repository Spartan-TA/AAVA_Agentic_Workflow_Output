package com.company.warehouse.attendance.service;

import org.springframework.stereotype.Service;

/**
 * Service for geofencing logic related to attendance events.
 */
@Service
public class GeofenceService {
    // Example warehouse geofence coordinates (latitude, longitude, radius in meters)
    private static final double WAREHOUSE_LATITUDE = 37.7749;
    private static final double WAREHOUSE_LONGITUDE = -122.4194;
    private static final double GEOFENCE_RADIUS_METERS = 200.0;

    /**
     * Checks if the given coordinates are within the warehouse geofence.
     * @param latitude Latitude of the event
     * @param longitude Longitude of the event
     * @return true if within geofence, false otherwise
     */
    public boolean isWithinGeofence(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return false;
        double distance = haversine(latitude, longitude, WAREHOUSE_LATITUDE, WAREHOUSE_LONGITUDE);
        return distance <= GEOFENCE_RADIUS_METERS;
    }

    // Haversine formula to calculate distance between two lat/lon points in meters
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the earth in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
