package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Warehouse Employee Management System.
 * 
 * This application provides comprehensive employee management capabilities including:
 * - Employee master data management (CRUD)
 * - Role-based access control (RBAC)
 * - Time and attendance tracking
 * - Shift and schedule management
 * - Leave and absence management
 * - Training and certification tracking
 * - Safety incident reporting
 * - Equipment and asset assignment
 * - Performance reviews and goals
 * - Payroll export integration
 * - Notifications and announcements
 * - Integration APIs for HRIS/WMS
 * - Audit trail and compliance
 * - Reporting and analytics
 * - Mobile PWA support
 * - Onboarding and offboarding workflows
 * - Multi-tenant localization
 * - Observability and monitoring
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class WarehouseEmsApplication {

    /**
     * Main entry point for the Spring Boot application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}