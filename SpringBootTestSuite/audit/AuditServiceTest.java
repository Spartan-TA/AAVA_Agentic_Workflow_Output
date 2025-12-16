import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class AuditServiceTest {
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAuditLog_ValidInput() {
        AuditLog log = new AuditLog("EMP123", "UPDATE", "name", "John", "Jon", LocalDateTime.now());
        when(auditLogRepository.save(any())).thenReturn(log);
        AuditLog result = auditService.createAuditLog(log);
        assertEquals("EMP123", result.getActorId());
        assertEquals("UPDATE", result.getAction());
    }

    @Test
    public void testCreateAuditLog_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> auditService.createAuditLog(null));
    }

    @Test
    public void testImmutableLogStorage() {
        AuditLog log = new AuditLog("EMP123", "DELETE", "status", "ACTIVE", "INACTIVE", LocalDateTime.now());
        when(auditLogRepository.save(any())).thenReturn(log);
        AuditLog saved = auditService.createAuditLog(log);
        assertTrue(saved.isImmutable());
    }

    @Test
    public void testBeforeAfterValueTracking() {
        AuditLog log = new AuditLog("EMP123", "UPDATE", "role", "Worker", "Supervisor", LocalDateTime.now());
        assertEquals("Worker", log.getBeforeValue());
        assertEquals("Supervisor", log.getAfterValue());
    }

    @Test
    public void testAuditQueryFilters_ByDateAndUser() {
        AuditLog log1 = new AuditLog("EMP123", "UPDATE", "role", "Worker", "Supervisor", LocalDateTime.now().minusDays(1));
        AuditLog log2 = new AuditLog("EMP124", "DELETE", "status", "ACTIVE", "INACTIVE", LocalDateTime.now());
        when(auditLogRepository.findByActorIdAndDateRange("EMP123", LocalDateTime.now().minusDays(2), LocalDateTime.now())).thenReturn(Arrays.asList(log1));
        List<AuditLog> logs = auditService.queryAuditLogs("EMP123", LocalDateTime.now().minusDays(2), LocalDateTime.now());
        assertEquals(1, logs.size());
        assertEquals("UPDATE", logs.get(0).getAction());
    }
}
