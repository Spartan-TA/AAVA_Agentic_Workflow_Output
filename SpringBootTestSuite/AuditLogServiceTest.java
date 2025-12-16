import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuditLogServiceTest {
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogAction_Valid() {
        AuditLog log = new AuditLog();
        log.setEntityType("Employee");
        log.setEntityId(1L);
        log.setAction("CREATE");
        log.setActor("admin");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(log);
        AuditLog result = auditLogService.logAction("Employee", 1L, "CREATE", "admin", null, "{}");
        assertNotNull(result);
        assertEquals("Employee", result.getEntityType());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void testLogAction_NullEntityType() {
        assertThrows(ValidationException.class, () -> auditLogService.logAction(null, 1L, "CREATE", "admin", null, "{}"));
    }

    @Test
    void testLogAction_NullAction() {
        assertThrows(ValidationException.class, () -> auditLogService.logAction("Employee", 1L, null, "admin", null, "{}"));
    }

    @Test
    void testLogAction_NullActor() {
        assertThrows(ValidationException.class, () -> auditLogService.logAction("Employee", 1L, "CREATE", null, null, "{}"));
    }

    @Test
    void testGetAuditLogsByEntity_Valid() {
        auditLogService.getAuditLogsByEntity("Employee", 1L);
        verify(auditLogRepository, times(1)).findByEntityTypeAndEntityId("Employee", 1L);
    }

    @Test
    void testGetAuditLogsByActor_Valid() {
        auditLogService.getAuditLogsByActor("admin");
        verify(auditLogRepository, times(1)).findByActor("admin");
    }

    @Test
    void testGetAuditLogsByDateRange_Valid() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        auditLogService.getAuditLogsByDateRange(start, end);
        verify(auditLogRepository, times(1)).findByTimestampBetween(start, end);
    }
}