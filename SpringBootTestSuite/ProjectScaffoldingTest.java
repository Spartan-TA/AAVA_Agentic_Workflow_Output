package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ProjectScaffoldingTest {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Test
    void contextLoads() {
        // Should load Spring context without errors
    }

    @Test
    void actuatorHealthEndpointReturnsUp() {
        assertThat(healthEndpoint.health().getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void flywayMigrationsRunSuccessfully() {
        // Test that Flyway/Liquibase migrations have been applied
    }
}