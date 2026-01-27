package com.companyname.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Warehouse Employee Management System
 * 
 * This application provides comprehensive employee management capabilities including:
 * - Employee CRUD operations with unique badge ID validation
 * - Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
 * - Time and attendance tracking with geofence validation
 * - Shift scheduling and management
 * - Leave and absence management
 * - Training and certification tracking
 * - Safety incident reporting
 * - Equipment and asset assignment
 * - Performance reviews and goals
 * - Payroll export integration
 * - Multi-channel notifications
 * - HRIS/WMS integration APIs
 * - Comprehensive audit logging
 * - Reporting and analytics
 * - PWA mobile support
 * - Onboarding and offboarding automation
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    
    /**
     * Main entry point for the Spring Boot application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}