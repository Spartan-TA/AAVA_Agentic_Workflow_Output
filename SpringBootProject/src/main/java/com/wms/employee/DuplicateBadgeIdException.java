package com.wms.employee;

public class DuplicateBadgeIdException extends RuntimeException {
    public DuplicateBadgeIdException(String message) {
        super(message);
    }
}
