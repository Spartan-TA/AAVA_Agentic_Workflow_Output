package com.warehouse.ems.employee.domain;

/**
 * Enum representing employee roles in the warehouse management system.
 * Used for role-based access control (RBAC) and authorization.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
public enum EmployeeRole {
    /**
     * Administrator with full system access.
     * Can manage all employees, configurations, and system settings.
     */
    ADMIN,
    
    /**
     * Human Resources personnel.
     * Can manage employee records, leave requests, and performance reviews.
     */
    HR,
    
    /**
     * Supervisor with team management capabilities.
     * Can manage their team's schedules, attendance, and performance.
     */
    SUPERVISOR,
    
    /**
     * Regular warehouse worker.
     * Can clock in/out, view schedules, and request leave.
     */
    WORKER
}