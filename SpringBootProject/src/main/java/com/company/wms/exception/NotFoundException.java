package com.company.wms.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Results in HTTP 404 response.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
public class NotFoundException extends RuntimeException {
    
    /**
     * Constructs a new NotFoundException with the specified detail message.
     * 
     * @param message the detail message
     */
    public NotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new NotFoundException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}