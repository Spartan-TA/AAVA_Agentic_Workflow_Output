package com.wms.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot Application for Warehouse Employee Management System.
 * Enables JPA Auditing for automatic tracking of entity changes.
 * Enables Async processing for notifications and background tasks.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}