package com.warehouse.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class E01_ProjectScaffoldingTest {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Test
    void contextLoads() {
        // Should load Spring context successfully
    }

    @Test
    void actuatorHealthEndpointReturnsUp() {
        assertThat(healthEndpoint.health().getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void flywayMigrationRunsSuccessfully() {
        // TODO: Verify Flyway/Liquibase baseline migration applied
    }
}