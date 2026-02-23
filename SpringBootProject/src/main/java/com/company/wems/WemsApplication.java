package com.company.wems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Warehouse Employee Management System (WEMS).
 * This application covers 20 epics including employee CRUD, RBAC, attendance, shift management, etc.
 *
 * @author WEMS Team
 */
@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}