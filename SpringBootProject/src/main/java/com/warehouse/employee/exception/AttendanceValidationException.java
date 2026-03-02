package com.warehouse.employee.exception;

public class AttendanceValidationException extends RuntimeException {
    public AttendanceValidationException(String message) {
        super(message);
    }
}