package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Warehouse Employee Management System (EMS)
 * Enables Spring Boot auto-configuration and component scanning
 * Enables scheduling for background jobs (HRIS/WMS sync)
 */
@SpringBootApplication
@EnableScheduling
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}