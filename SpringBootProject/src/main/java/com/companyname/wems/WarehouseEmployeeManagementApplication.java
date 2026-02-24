package com.companyname.wems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the Warehouse Employee Management System Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.companyname.wems.domain")
@EnableJpaRepositories(basePackages = "com.companyname.wems.repository")
@ComponentScan(basePackages = "com.companyname.wems")
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
