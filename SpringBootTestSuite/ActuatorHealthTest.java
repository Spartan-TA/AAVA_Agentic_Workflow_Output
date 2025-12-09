import org.junit.jupiter.api.*;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ActuatorHealthTest {
    private HealthIndicator healthIndicator;

    @BeforeEach
    public void setUp() {
        healthIndicator = mock(HealthIndicator.class);
    }

    @AfterEach
    public void tearDown() {
        healthIndicator = null;
    }

    @Test
    public void testHealthUp() {
        when(healthIndicator.health()).thenReturn(Health.up().build());
        Health health = healthIndicator.health();
        assertEquals("UP", health.getStatus().getCode());
    }

    @Test
    public void testHealthDown() {
        when(healthIndicator.health()).thenReturn(Health.down().build());
        Health health = healthIndicator.health();
        assertEquals("DOWN", health.getStatus().getCode());
    }

    @Test
    public void testHealthWithDetails() {
        when(healthIndicator.health()).thenReturn(Health.up().withDetail("db", "ok").build());
        Health health = healthIndicator.health();
        assertEquals("UP", health.getStatus().getCode());
        assertEquals("ok", health.getDetails().get("db"));
    }

    @Test
    public void testHealthNullResponse() {
        when(healthIndicator.health()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> {
            Health health = healthIndicator.health();
            health.getStatus();
        });
    }
}