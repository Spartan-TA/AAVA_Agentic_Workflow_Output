package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * Main entry point for Warehouse Employee Management System Spring Boot Application.
 */
@SpringBootApplication
@EntityScan(basePackages = {"com.warehouse.ems.domain"})
@EnableJpaRepositories(basePackages = {"com.warehouse.ems.repository"})
@EnableScheduling
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
