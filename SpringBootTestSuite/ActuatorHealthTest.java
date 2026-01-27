package com.warehouse.ems.actuator;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ActuatorHealthTest {
    @Autowired
    HealthIndicator healthIndicator;

    @MockBean
    MetricsService metricsService;
    @MockBean
    FlywayMigrationService flywayMigrationService;

    @Test
    void testHealthEndpoint_UP() {
        when(healthIndicator.health().getStatus().getCode()).thenReturn("UP");
        assertEquals("UP", healthIndicator.health().getStatus().getCode());
    }

    @Test
    void testHealthEndpoint_DOWN() {
        when(healthIndicator.health().getStatus().getCode()).thenReturn("DOWN");
        assertEquals("DOWN", healthIndicator.health().getStatus().getCode());
    }

    @Test
    void testMetricsEndpoint() {
        when(metricsService.getMetric("attendance.count")).thenReturn(100);
        int count = metricsService.getMetric("attendance.count");
        assertEquals(100, count);
    }

    @Test
    void testFlywayMigration_Valid() {
        when(flywayMigrationService.isMigrationValid()).thenReturn(true);
        assertTrue(flywayMigrationService.isMigrationValid());
    }

    @Test
    void testFlywayMigration_Invalid() {
        when(flywayMigrationService.isMigrationValid()).thenReturn(false);
        assertFalse(flywayMigrationService.isMigrationValid());
    }

    @Test
    void testNullMetric_Throws() {
        assertThrows(IllegalArgumentException.class, () -> metricsService.getMetric(null));
    }

    @Test
    void testIntegration_HealthAndMetrics() {
        when(healthIndicator.health().getStatus().getCode()).thenReturn("UP");
        when(metricsService.getMetric("overtime.count")).thenReturn(20);
        assertEquals("UP", healthIndicator.health().getStatus().getCode());
        assertEquals(20, metricsService.getMetric("overtime.count"));
    }
}
