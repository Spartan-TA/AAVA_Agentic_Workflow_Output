package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Warehouse Employee Management System Spring Boot application.
 *
 * <p>Bootstraps the application and enables component scanning for all modules.</p>
 */
@SpringBootApplication
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
