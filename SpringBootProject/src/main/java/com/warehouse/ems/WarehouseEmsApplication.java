package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application class for Warehouse Employee Management System.
 * 
 * This application provides comprehensive employee management capabilities including:
 * - Employee master data management (CRUD operations)
 * - Role-based access control (RBAC) with Spring Security
 * - Time and attendance tracking (clock-in/out)
 * - Shift and schedule management
 * - Leave and absence management
 * - Training and certification tracking
 * - Safety incident reporting and OSHA compliance
 * - Equipment and asset assignment
 * - Performance reviews and goals
 * - Payroll export integration
 * - Notifications and announcements
 * - Integration layer for HRIS/WMS APIs
 * - Audit trail and compliance logging
 * - Reporting and analytics
 * - Mobile access via PWA
 * - Onboarding and offboarding workflows
 * 
 * @author Warehouse EMS Development Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class WarehouseEmsApplication {

    /**
     * Main entry point for the Spring Boot application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
        System.out.println("
" +
            "=================================================
" +
            "  Warehouse EMS Application Started Successfully
" +
            "  Port: 8080
" +
            "  Health Check: http://localhost:8080/actuator/health
" +
            "  API Docs: http://localhost:8080/swagger-ui.html
" +
            "  H2 Console: http://localhost:8080/h2-console
" +
            "=================================================
");
    }
}