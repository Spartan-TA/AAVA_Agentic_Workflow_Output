package com.warehouse.management.employee.exception;

/**
 * Exception thrown when a duplicate badgeId is detected.
 */
public class DuplicateBadgeIdException extends RuntimeException {
    public DuplicateBadgeIdException(String badgeId) {
        super("Employee with badgeId '" + badgeId + "' already exists.");
    }
}
