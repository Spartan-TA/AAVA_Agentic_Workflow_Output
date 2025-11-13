package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Warehouse Energy Management System.
 * 
 * This Spring Boot application provides comprehensive energy monitoring,
 * tracking, optimization, and reporting capabilities for warehouse operations.
 * 
 * Key Features:
 * - Real-time energy monitoring dashboard
 * - Equipment-level energy consumption tracking
 * - Automated energy optimization
 * - Comprehensive reporting and analytics
 * - Alert and notification system
 * - Multi-warehouse management
 * - Compliance and sustainability reporting
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableScheduling
@EnableAsync
public class WarehouseEmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}