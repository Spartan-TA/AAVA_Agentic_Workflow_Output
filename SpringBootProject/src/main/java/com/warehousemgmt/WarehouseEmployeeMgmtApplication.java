package com.warehousemgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Main entry point for Warehouse Employee Management System.
 * Enables Actuator and OpenAPI documentation.
 */
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }

    /**
     * Customizes Prometheus metrics registry for observability.
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "WarehouseEmployeeMgmt");
    }
}
