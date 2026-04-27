package com.company.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for Warehouse Employee Management System.
 * Enables caching, scheduling, and transaction management.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableTransactionManagement
public class WarehouseManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagementApplication.class, args);
    }
}