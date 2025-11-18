package com.warehouse.employee.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for Warehouse Employee Management System.
 * Enables JPA Auditing, Async, Scheduling, Caching, and Transaction Management.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
