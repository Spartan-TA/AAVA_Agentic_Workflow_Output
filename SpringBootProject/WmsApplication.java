package com.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Warehouse Employee Management System.
 * This class serves as the entry point for the application.
 * 
 * @SpringBootApplication enables:
 * - Component scanning
 * - Auto-configuration
 * - Property support
 */
@SpringBootApplication
public class WmsApplication {
    
    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
    }
}
