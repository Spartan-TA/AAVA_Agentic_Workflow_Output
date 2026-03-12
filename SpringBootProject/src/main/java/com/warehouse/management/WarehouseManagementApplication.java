package com.warehouse.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for Warehouse Employee Management System.
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableJpaAuditing
public class WarehouseManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagementApplication.class, args);
    }
}
