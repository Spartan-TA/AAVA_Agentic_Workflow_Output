package com.wms.attendance.service;

import org.springframework.stereotype.Service;

/**
 * Service for geofencing logic (validating employee location for attendance events).
 */
@Service
public class GeofenceService {
    // Example warehouse coordinates (latitude, longitude, radius in meters)
    private static final double WAREHOUSE_LAT = 40.7128;
    private static final double WAREHOUSE_LON = -74.0060;
    private static final double RADIUS_METERS = 200.0;

    /**
     * Checks if the given coordinates are within the warehouse geofence.
     * @param lat Latitude
     * @param lon Longitude
     * @return true if inside geofence, false otherwise
     */
    public boolean isWithinGeofence(double lat, double lon) {
        double distance = haversine(lat, lon, WAREHOUSE_LAT, WAREHOUSE_LON);
        return distance <= RADIUS_METERS;
    }

    /**
     * Haversine formula to calculate distance between two lat/lon points in meters.
     */
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
