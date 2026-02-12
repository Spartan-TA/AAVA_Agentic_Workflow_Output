package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Warehouse Employee Management System.
 * 
 * This Spring Boot application provides comprehensive employee management capabilities including:
 * - Employee CRUD operations with RBAC
 * - Time & Attendance tracking with clock in/out
 * - Shift scheduling and management
 * - Leave request and approval workflows
 * - Certification tracking with expiry alerts
 * - Safety incident recording and OSHA reporting
 * - Equipment assignment with validation
 * - Performance review workflows
 * - Payroll export integration
 * - Notifications and announcements
 * - HRIS/WMS integration layer
 * - Comprehensive audit trail
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}