package com.warehouse.employee.exception;

public class ShiftConflictException extends RuntimeException {
    public ShiftConflictException(String message) {
        super(message);
    }
}
