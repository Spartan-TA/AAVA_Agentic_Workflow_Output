package com.company.wms.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Results in HTTP 409 Conflict response.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
public class DuplicateResourceException extends RuntimeException {
    
    /**
     * Constructs a new DuplicateResourceException with the specified detail message.
     * 
     * @param message the detail message
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DuplicateResourceException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}