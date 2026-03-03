package com.company.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Warehouse Employee Management System.
 * This Spring Boot application manages employee data, scheduling, attendance,
 * safety, certifications, and more for warehouse operations.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class WarehouseEmployeeManagementApplication {
    
    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}