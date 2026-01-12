package com.warehouse.ems.employee.domain;

/**
 * Enum representing the employment status of an employee.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
public enum EmployeeStatus {
    /**
     * Employee is actively working.
     */
    ACTIVE,
    
    /**
     * Employee is temporarily inactive (e.g., suspended).
     */
    INACTIVE,
    
    /**
     * Employee is on approved leave (PTO, sick, etc.).
     */
    ON_LEAVE,
    
    /**
     * Employee has been terminated.
     */
    TERMINATED
}