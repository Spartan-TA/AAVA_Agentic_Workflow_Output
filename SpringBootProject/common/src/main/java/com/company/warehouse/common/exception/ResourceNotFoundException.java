package com.company.warehouse.common.exception;

/**
 * Exception for resource not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}