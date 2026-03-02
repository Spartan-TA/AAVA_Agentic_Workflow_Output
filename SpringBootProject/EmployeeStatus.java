package com.wms.employee;

/**
 * Enumeration of possible employee statuses in the warehouse management system.
 * 
 * Status Definitions:
 * - ACTIVE: Employee is currently working and available for scheduling
 * - INACTIVE: Employee is temporarily not working (e.g., suspended, on unpaid leave)
 * - ON_LEAVE: Employee is on approved leave (PTO, sick leave, etc.)
 * - TERMINATED: Employee has been terminated and should not be scheduled
 */
public enum EmployeeStatus {
    ACTIVE,
    INACTIVE,
    ON_LEAVE,
    TERMINATED
}
