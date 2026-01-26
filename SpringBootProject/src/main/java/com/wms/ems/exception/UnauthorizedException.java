package com.wms.ems.exception;

/**
 * Exception thrown when a user is unauthorized to access a resource.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
