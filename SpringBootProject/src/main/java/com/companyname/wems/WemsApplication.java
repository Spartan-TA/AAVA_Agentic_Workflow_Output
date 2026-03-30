package com.companyname.wems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Warehouse Employee Management System (WEMS)
 * 
 * This Spring Boot application provides comprehensive employee management capabilities including:
 * - Employee master data management
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
 * 
 * @author WEMS Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class WemsApplication {
    
    /**
     * Main entry point for the Spring Boot application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}