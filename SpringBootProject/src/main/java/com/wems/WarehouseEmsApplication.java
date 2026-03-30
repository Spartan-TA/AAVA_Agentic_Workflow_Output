package com.wems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Warehouse Employee Management System (EMS).
 * Handles application startup and configuration.
 */
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        // Start the Spring Boot application
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
