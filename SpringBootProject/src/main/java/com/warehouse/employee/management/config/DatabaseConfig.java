package com.warehouse.employee.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.warehouse.employee.management")
public class DatabaseConfig {
    // Additional database configuration if needed
}
