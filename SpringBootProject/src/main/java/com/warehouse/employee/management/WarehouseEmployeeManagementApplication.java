package com.warehouse.employee.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main entry point for the Warehouse Employee Management System Spring Boot application.
 */
@SpringBootApplication
@EnableConfigurationProperties
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
