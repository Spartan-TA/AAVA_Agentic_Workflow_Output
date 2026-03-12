package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for Warehouse Employee Management System Spring Boot Application.
 */
@SpringBootApplication(scanBasePackages = "com.warehouse.ems")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
