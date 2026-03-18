package com.warehouse.employee.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for Warehouse Employee Management System.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableScheduling
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}