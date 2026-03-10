package com.company.warehouse.common.exception;

/**
 * Exception for bad requests.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}