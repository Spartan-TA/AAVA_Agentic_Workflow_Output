package com.warehouse.employeemgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Warehouse Employee Management System
 * 
 * This application provides comprehensive employee management capabilities including:
 * - Employee CRUD operations with RBAC
 * - Time & Attendance tracking
 * - Shift & Schedule management
 * - Leave & Absence management
 * - Training & Certification tracking
 * - Safety Incidents & OSHA reporting
 * - Equipment & Asset assignment
 * - Performance Reviews & Goals
 * - Payroll Export integration
 * - Notifications & Announcements
 * - HRIS/WMS API integration
 * - Audit Trail & Compliance
 * - Reporting & Analytics
 * - Mobile PWA access
 * - Onboarding & Offboarding workflows
 * - Multi-tenant & Localization support
 * - Observability & Monitoring
 * - CI/CD automation
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}