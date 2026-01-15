package com.company.wms.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Employee module of Warehouse Employee Management System.
 */
@SpringBootApplication(scanBasePackages = {"com.company.wms.employee", "com.company.wms.common"})
public class EmployeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeApplication.class, args);
    }
}
