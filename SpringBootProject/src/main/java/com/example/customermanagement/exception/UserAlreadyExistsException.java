package com.example.customermanagement.exception;

/**
 * Exception thrown when a user already exists during registration.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
