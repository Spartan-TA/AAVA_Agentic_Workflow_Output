package com.warehouse.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for Warehouse Employee Management System.
 * Modules: Employee, Scheduling, Attendance, Safety, etc.
 */
@SpringBootApplication(scanBasePackages = {
        "com.warehouse.employee",
        "com.warehouse.scheduling",
        "com.warehouse.attendance",
        "com.warehouse.safety"
})
public class EmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}
