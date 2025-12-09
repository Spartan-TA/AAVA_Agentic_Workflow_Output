import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuditLogServiceTest {
    private AuditLogService service;

    @BeforeEach
    public void setUp() {
        service = new AuditLogService();
    }

    @Test
    public void testLogAuditEvent_Valid() {
        AuditEvent event = new AuditEvent("emp1", "LOGIN", "2024-07-01T08:00:00Z");
        assertDoesNotThrow(() -> service.logAuditEvent(event));
    }

    @Test
    public void testLogAuditEvent_NullEvent() {
        assertThrows(IllegalArgumentException.class, () -> service.logAuditEvent(null));
    }

    @Test
    public void testSearchAuditLogs_ValidFilters() {
        assertNotNull(service.searchAuditLogs("LOGIN", "2024-07-01", "2024-07-31"));
    }

    @Test
    public void testSearchAuditLogs_EmptyFilters() {
        assertThrows(IllegalArgumentException.class, () -> service.searchAuditLogs("", "", ""));
    }

    @Test
    public void testExportAuditLogs_ValidFormat() {
        assertDoesNotThrow(() -> service.exportAuditLogs("csv"));
    }

    @Test
    public void testExportAuditLogs_InvalidFormat() {
        assertThrows(InvalidFormatException.class, () -> service.exportAuditLogs("xml"));
    }

    @Test
    public void testTriggerComplianceAlert_Valid() {
        assertTrue(service.triggerComplianceAlert("emp2", "FAILED_LOGIN"));
    }

    @Test
    public void testTriggerComplianceAlert_InvalidEvent() {
        assertThrows(IllegalArgumentException.class, () -> service.triggerComplianceAlert("emp3", ""));
    }

    @Test
    public void testGetAuditTrail_Valid() {
        assertNotNull(service.getAuditTrail("emp4"));
    }

    @Test
    public void testGetAuditTrail_ImmutableStorage() {
        AuditTrail trail = service.getAuditTrail("emp5");
        assertTrue(trail.isImmutable());
    }
}