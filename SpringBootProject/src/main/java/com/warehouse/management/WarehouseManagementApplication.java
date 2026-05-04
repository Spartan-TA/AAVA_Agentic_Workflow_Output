package com.warehouse.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Warehouse Employee Management System.
 * 
 * This Spring Boot application provides comprehensive employee management capabilities including:
 * - Employee master data management (CRUD)
 * - Role-based access control (RBAC)
 * - Time and attendance tracking
 * - Shift and schedule management
 * - Leave and absence management
 * - Training and certification tracking
 * - Safety incident reporting
 * - Equipment and asset assignment
 * - Performance reviews
 * - Payroll integration
 * - Notifications and announcements
 * - HRIS/WMS integration
 * - Audit trail and compliance
 * - Reporting and analytics
 * - Mobile PWA support
 * - Onboarding/offboarding workflows
 * - Localization and multi-tenancy
 * - Advanced scheduling optimization
 * - Self-service portal
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class WarehouseManagementApplication {

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagementApplication.class, args);
    }
}