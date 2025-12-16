package com.warehouse.management.employee.exception;

/**
 * Exception thrown when an Employee is not found.
 */
public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }
    public EmployeeNotFoundException(String badgeId) {
        super("Employee not found with badgeId: " + badgeId);
    }
}
