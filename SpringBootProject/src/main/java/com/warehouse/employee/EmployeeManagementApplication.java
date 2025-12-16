package com.warehouse.employee;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Warehouse Employee Management System.
 */
@SpringBootApplication(scanBasePackages = "com.warehouse.employee")
@OpenAPIDefinition(info = @Info(title = "Warehouse Employee Management API", version = "1.0", description = "Comprehensive warehouse employee management system"))
public class EmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}
