package com.wms.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main entry point for Warehouse Employee Management System.
 * Runs Spring Boot application and enables configuration properties.
 */
@SpringBootApplication
@EnableConfigurationProperties
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
