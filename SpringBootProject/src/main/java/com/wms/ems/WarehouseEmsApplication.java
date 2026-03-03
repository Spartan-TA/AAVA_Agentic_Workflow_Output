package com.wms.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Warehouse Employee Management System Spring Boot application.
 * <p>
 * This application manages warehouse employee operations, including authentication,
 * employee records, and operational workflows.
 * </p>
 *
 * @author Warehouse EMS Team
 * @since 1.0.0
 */
@SpringBootApplication
public class WarehouseEmsApplication {

    /**
     * Starts the Warehouse Employee Management System application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
