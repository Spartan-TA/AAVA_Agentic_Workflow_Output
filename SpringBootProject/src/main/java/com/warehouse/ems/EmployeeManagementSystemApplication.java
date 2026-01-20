package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for Warehouse Employee Management System.
 * This Spring Boot application manages all aspects of warehouse employee operations
 * including attendance, scheduling, safety, assets, payroll, and more.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
@SpringBootApplication
public class EmployeeManagementSystemApplication {
    
    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementSystemApplication.class, args);
    }
}