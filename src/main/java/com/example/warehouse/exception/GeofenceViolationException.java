package com.example.warehouse.exception;

public class GeofenceViolationException extends RuntimeException {
    public GeofenceViolationException(String message) {
        super(message);
    }
}
