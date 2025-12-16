package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for Warehouse Employee Management System.
 * This class bootstraps the entire application and enables auto-configuration.
 * 
 * @author Warehouse Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class EmployeeMgmtApplication {
    
    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EmployeeMgmtApplication.class, args);
    }
}