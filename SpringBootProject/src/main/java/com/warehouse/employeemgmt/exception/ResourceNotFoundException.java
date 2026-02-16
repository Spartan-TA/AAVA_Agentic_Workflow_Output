package com.warehouse.employeemgmt.exception;

/**
 * ResourceNotFoundException - Custom exception for resource not found scenarios
 * 
 * Thrown when a requested resource (employee, attendance, etc.) is not found.
 * Handled by GlobalExceptionHandler to return appropriate HTTP 404 responses.
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}