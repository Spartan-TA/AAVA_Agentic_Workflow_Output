package com.warehouse.employee.exception;

public class DuplicateBadgeIdException extends RuntimeException {
    public DuplicateBadgeIdException(String message) {
        super(message);
    }
}