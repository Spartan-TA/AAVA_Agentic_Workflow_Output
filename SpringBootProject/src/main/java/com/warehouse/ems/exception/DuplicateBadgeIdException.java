package com.warehouse.ems.exception;

public class DuplicateBadgeIdException extends RuntimeException {
    public DuplicateBadgeIdException(String message) {
        super(message);
    }
}