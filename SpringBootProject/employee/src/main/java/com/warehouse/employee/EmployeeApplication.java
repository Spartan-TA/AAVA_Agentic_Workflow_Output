package com.warehouse.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Employee module Spring Boot application.
 */
@SpringBootApplication(scanBasePackages = {"com.warehouse.employee", "com.warehouse.core"})
public class EmployeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeApplication.class, args);
    }
}
