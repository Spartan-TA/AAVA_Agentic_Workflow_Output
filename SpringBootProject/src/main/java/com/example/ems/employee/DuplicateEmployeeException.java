package com.example.ems.employee;

/**
 * Exception thrown when an employee with duplicate email is created.
 */
public class DuplicateEmployeeException extends RuntimeException {
    public DuplicateEmployeeException(String message) {
        super(message);
    }
}
