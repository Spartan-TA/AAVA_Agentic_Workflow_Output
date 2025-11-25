package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Warehouse Employee Management System (EMS) Spring Boot application.
 * Enables caching for performance optimization.
 */
@SpringBootApplication
@EnableCaching
public class EmsApplication {
    /**
     * Main method to launch the Spring Boot application.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
