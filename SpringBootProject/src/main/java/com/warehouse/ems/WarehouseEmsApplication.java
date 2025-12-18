package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the Warehouse Employee Management System Spring Boot application.
 * Scans all sub-packages for Spring components.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.warehouse.ems"})
public class WarehouseEmsApplication {
    /**
     * Application main method.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
