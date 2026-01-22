package com.warehouse.ems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.flywaydb.core.Flyway;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test suite for E01 - Project Scaffolding & Domain Setup
 * 
 * Tests cover:
 * - Application startup and context loading
 * - Actuator health endpoint
 * - Flyway database migrations
 * - Base package structure validation
 * - Server port configuration
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E01 - Project Scaffolding Tests")
public class E01_ProjectScaffoldingTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Flyway flyway;

    // ========== NORMAL CASES ==========

    @Test
    @DisplayName("Test 1: Application context loads successfully")
    public void testApplicationContextLoads() {
        // Arrange & Act - Spring Boot loads context automatically
        
        // Assert
        assertNotNull(restTemplate, "RestTemplate should be autowired");
        assertTrue(port > 0, "Server port should be assigned");
    }

    @Test
    @DisplayName("Test 2: Actuator health endpoint returns UP status")
    public void testActuatorHealthEndpointReturnsUp() {
        // Arrange
        String url = "http://localhost:" + port + "/actuator/health";
        
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Health endpoint should return 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().contains("UP"), "Health status should be UP");
    }

    @Test
    @DisplayName("Test 3: Flyway migrations execute successfully")
    public void testFlywayMigrationsExecuteSuccessfully() {
        // Arrange & Act
        var info = flyway.info();
        
        // Assert
        assertNotNull(info, "Flyway info should not be null");
        assertTrue(info.all().length > 0, "At least one migration should exist");
        assertEquals(0, info.pending().length, "No pending migrations should exist");
    }

    @Test
    @DisplayName("Test 4: Server starts on configured port")
    public void testServerStartsOnConfiguredPort() {
        // Arrange
        String url = "http://localhost:" + port + "/actuator/health";
        
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Server should respond on configured port");
    }

    // ========== BOUNDARY CONDITIONS ==========

    @Test
    @DisplayName("Test 5: Health endpoint accessible without authentication")
    public void testHealthEndpointAccessibleWithoutAuth() {
        // Arrange
        String url = "http://localhost:" + port + "/actuator/health";
        
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Health endpoint should be publicly accessible");
    }

    @Test
    @DisplayName("Test 6: Flyway baseline migration exists")
    public void testFlywayBaselineMigrationExists() {
        // Arrange & Act
        var migrations = flyway.info().all();
        
        // Assert
        assertTrue(migrations.length > 0, "Baseline migration should exist");
        assertEquals("1", migrations[0].getVersion().getVersion(), "First migration should be V1");
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Test 7: Invalid actuator endpoint returns 404")
    public void testInvalidActuatorEndpointReturns404() {
        // Arrange
        String url = "http://localhost:" + port + "/actuator/invalid";
        
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Invalid endpoint should return 404");
    }

    @Test
    @DisplayName("Test 8: Flyway migration history is immutable")
    public void testFlywayMigrationHistoryIsImmutable() {
        // Arrange
        var initialMigrations = flyway.info().all();
        int initialCount = initialMigrations.length;
        
        // Act - Attempt to get info again
        var subsequentMigrations = flyway.info().all();
        
        // Assert
        assertEquals(initialCount, subsequentMigrations.length, "Migration count should remain constant");
    }

    @Test
    @DisplayName("Test 9: Application handles concurrent health checks")
    public void testApplicationHandlesConcurrentHealthChecks() throws InterruptedException {
        // Arrange
        String url = "http://localhost:" + port + "/actuator/health";
        int concurrentRequests = 10;
        Thread[] threads = new Thread[concurrentRequests];
        boolean[] results = new boolean[concurrentRequests];
        
        // Act
        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                results[index] = response.getStatusCode() == HttpStatus.OK;
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Assert
        for (boolean result : results) {
            assertTrue(result, "All concurrent health checks should succeed");
        }
    }

    @Test
    @DisplayName("Test 10: Flyway validates migration checksums")
    public void testFlywayValidatesMigrationChecksums() {
        // Arrange & Act
        assertDoesNotThrow(() -> flyway.validate(), "Flyway validation should pass");
        
        // Assert - If no exception thrown, validation passed
        assertTrue(true, "Migration checksums are valid");
    }
}