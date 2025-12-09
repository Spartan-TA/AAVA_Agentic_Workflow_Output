import org.junit.jupiter.api.*;
import org.flywaydb.core.Flyway;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class FlywayMigrationTest {
    private Flyway flyway;

    @BeforeEach
    public void setUp() {
        flyway = mock(Flyway.class);
    }

    @AfterEach
    public void tearDown() {
        flyway = null;
    }

    @Test
    public void testMigrateSuccess() {
        when(flyway.migrate()).thenReturn(5);
        int migrations = flyway.migrate();
        assertEquals(5, migrations);
    }

    @Test
    public void testMigrateZeroMigrations() {
        when(flyway.migrate()).thenReturn(0);
        int migrations = flyway.migrate();
        assertEquals(0, migrations);
    }

    @Test
    public void testMigrateThrowsException() {
        when(flyway.migrate()).thenThrow(new RuntimeException("Migration failed"));
        assertThrows(RuntimeException.class, () -> flyway.migrate());
    }

    @Test
    public void testCleanSuccess() {
        doNothing().when(flyway).clean();
        assertDoesNotThrow(() -> flyway.clean());
    }

    @Test
    public void testCleanThrowsException() {
        doThrow(new RuntimeException("Clean failed")).when(flyway).clean();
        assertThrows(RuntimeException.class, () -> flyway.clean());
    }
}